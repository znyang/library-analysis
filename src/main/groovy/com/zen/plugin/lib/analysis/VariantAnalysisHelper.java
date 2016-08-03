package com.zen.plugin.lib.analysis;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.annotations.VisibleForTesting;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.builder.dependency.JarDependency;
import com.android.builder.dependency.LibraryDependency;
import com.zen.plugin.lib.analysis.comparator.SizeComparator;
import com.zen.plugin.lib.analysis.log.ILog;
import com.zen.plugin.lib.analysis.model.FileWrapper;
import com.zen.plugin.lib.analysis.model.GraphNode;
import com.zen.plugin.lib.analysis.model.Library;
import com.zen.plugin.lib.analysis.model.Node;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.result.ResolutionResult;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableDependency;
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableModuleResult;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 对给定的变种版本进行依赖树建立，输出到Library中，并且在分析后就能计算出各依赖组件的文件占用大小
 *
 * @author zen
 * @version 2016/6/5
 */
public final class VariantAnalysisHelper {

    public static Library analysis(BaseVariantData variantData, List<String> ignore, long limitSize) throws IOException {
        List<LibraryDependency> libraries =
                variantData.getVariantConfiguration().getDirectLibraries();
        Library lib = doAnalysis(libraries, variantData.getVariantDependency().getLocalDependencies(), ignore, limitSize);
        if (lib != null) {
            lib.setName(variantData.getApplicationId());
        }
        return lib;
    }

    public static SortedSet<FileWrapper> analysis(Project project, Configuration configuration, final ILog logger) {
//        logger.d("---- AllDependencies ----");
//        DependencySet allDependencies = configuration.getAllDependencies();
//        for (Dependency dependency : allDependencies) {
//            logger.d(dependency.toString());
//        }
//        ResolutionResult result = configuration.getIncoming().getResolutionResult();
//        RenderableDependency root = new RenderableModuleResult(result.getRoot());
//        ILog log = new Logger();
//        log.d("start");
//        logDependencyResult(log, root);
//        new LogReportRenderer("dependencies.txt", project.getBuildDir().getPath())
//                .renderLog(log);


        logger.d("---- AllFiles ----");
        SortedSet<FileWrapper> fileWrappers = new TreeSet<>(new SizeComparator());
        FileCollection files = configuration.getIncoming().getFiles();
        for (File file : files) {
            logger.d(file.getName() + " " + FileUtils.convertFileSize(file.length()));
            if (file.getName().endsWith(".jar")) {
                fileWrappers.add(new FileWrapper(file.getName(), file.getName(), file.length()));
            }
        }
        return fileWrappers;
    }

    public static GraphNode convertGraphNode(Node node) {
        return convertGraphNode(new HashMap<String, GraphNode>(), node);
    }

    private static GraphNode convertGraphNode(@NonNull Map<String, GraphNode> nodes, Node node) {
        GraphNode graphNode;
        if (!nodes.containsKey(node.getId())) {
            graphNode = new GraphNode(node.getId());
            nodes.put(graphNode.getName(), graphNode);
            graphNode.setDag(nodes);
        } else {
            return nodes.get(node.getId());
        }

        List<Node> children = node.getChildren();
        if (children == null || children.isEmpty()) {
            return graphNode;
        }
        for (Node child : children) {
            graphNode.addChild(convertGraphNode(nodes, child));
        }
        return graphNode;
    }

    public static Node convertDependencyNode(ResolutionResult result) {
        return convertDependencyNode(new HashSet<>(), new RenderableModuleResult(result.getRoot()));
    }

    public static Node convertAllDependencyNode(ResolutionResult result) {
        return convertDependencyNode(null, new RenderableModuleResult(result.getRoot()));
    }

    /**
     * @param collection 收集已出现过的依赖库，如果为null表示不收集，添加所有节点
     * @param result     要解析的依赖库
     * @return 节点对象
     */
    private static Node convertDependencyNode(Set<Object> collection, RenderableDependency result) {
        if (result == null) {
            return null;
        }

        // 是否已经加入过
        boolean hasAdded = collection != null && collection.contains(result.getId());
        Set<? extends RenderableDependency> children = result.getChildren();
        boolean hasChildren = children != null && !children.isEmpty();

        Node node = new Node();
        node.setId(result.getName());
        // 已加入过并且有子节点，添加一个标记#
        node.setName(hasAdded && hasChildren ? "+ " + result.getName() : result.getName());

        if (!hasAdded) {
            if (collection != null) {
                collection.add(result.getId());
            }

            if (hasChildren) {
                for (RenderableDependency dependency : children) {
                    node.addNode(convertDependencyNode(collection, dependency));
                }
            }
        }
        node.setOpen(!hasAdded);
        return node;
    }

    private static void logDependencyResult(ILog logger, RenderableDependency result) {
        if (result == null) {
            return;
        }
        Set<? extends RenderableDependency> children = result.getChildren();
        for (RenderableDependency dependency : children) {
            logger.d(dependency.getName() + " " + dependency.getId());
            logDependencyResult(logger, dependency);
        }
    }

    static Library doAnalysis(@NonNull List<LibraryDependency> libraries,
                              @Nullable List<JarDependency> localJars,
                              List<String> ignore,
                              long limitSize) {
        if (libraries.isEmpty() && (localJars == null || localJars.isEmpty())) {
            return null;
        }

        Library library = new Library();
        analysisChildren(library, libraries, localJars);
        // analysis dependencies file size
        library.computeDependencies(true, ignore, limitSize);
        return library;
    }

    @VisibleForTesting
    private static void analysisChildren(@NonNull Library library,
                                         @NonNull List<LibraryDependency> libraries,
                                         @Nullable Collection<JarDependency> localJars) {
        if (localJars != null) {
            for (JarDependency jarDependency : localJars) {
                library.getJarDependencies().add(jarDependency);
            }
        }

        final int count = libraries.size();
        for (int i = 0; i < count; i++) {
            LibraryDependency libDependency = libraries.get(i);
            if (libDependency != null) {
                createDependency(library, libDependency, i == count - 1);
            }
        }
    }

    private static void createDependency(@NonNull Library library,
                                         @NonNull final LibraryDependency lib,
                                         boolean lastChild) {
        Library libNew = new Library();
        libNew.setLibraryDependency(lib);
        libNew.setLast(lastChild);

        analysisChildren(libNew, lib.getDependencies(), lib.getLocalDependencies());
        library.getLibraries().add(libNew);
    }

}
