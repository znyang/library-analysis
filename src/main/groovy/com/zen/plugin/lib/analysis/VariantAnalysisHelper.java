package com.zen.plugin.lib.analysis;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.annotations.VisibleForTesting;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.builder.dependency.JarDependency;
import com.android.builder.dependency.LibraryDependency;
import com.zen.plugin.lib.analysis.log.ILog;
import com.zen.plugin.lib.analysis.model.Library;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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

    public static Library analysis(Configuration configuration, ILog logger) {
        logDependencyResult(logger, configuration.getIncoming().getResolutionResult().getRoot());
        return null;
    }

    private static void logDependencyResult(ILog logger, ResolvedComponentResult result) {
        if (result == null) {
            return;
        }
        Set<? extends DependencyResult> dependencySet = result.getDependencies();
        for (DependencyResult dependency : dependencySet) {
            logger.d(dependency.toString());
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
