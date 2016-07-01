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
import com.zen.plugin.lib.analysis.model.Library;

import org.apache.commons.io.comparator.SizeFileComparator;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
import org.gradle.api.artifacts.result.UnresolvedDependencyResult;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency;
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableDependency;
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableDependencyResult;
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableModuleResult;
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableUnresolvedDependencyResult;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

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

        return doAnalysis(libraries, variantData.getVariantDependency().getLocalDependencies(), ignore, limitSize);
    }

    public static SortedSet<FileWrapper> analysis(Configuration configuration, final ILog logger) {
        logger.d("---- AllDependencies ----");
        DependencySet allDependencies = configuration.getAllDependencies();
        for (Dependency dependency : allDependencies) {
            logger.d(dependency.toString());
        }

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
