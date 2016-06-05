package com.zen.plugin.lib.analysis;

import com.android.annotations.NonNull;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.builder.dependency.JarDependency;
import com.android.builder.dependency.LibraryDependency;
import com.zen.plugin.lib.analysis.model.Library;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.tasks.diagnostics.internal.TextReportRenderer;
import org.gradle.internal.graph.GraphRenderer;
import org.gradle.logging.StyledTextOutput;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.gradle.logging.StyledTextOutput.Style.Description;
import static org.gradle.logging.StyledTextOutput.Style.Failure;
import static org.gradle.logging.StyledTextOutput.Style.Identifier;
import static org.gradle.logging.StyledTextOutput.Style.Info;
import static org.gradle.logging.StyledTextOutput.Style.Normal;

/**
 * @author zen
 * @version 2016/6/4
 */
public class LibraryReportRenderer extends TextReportRenderer {

    private boolean                  hasConfigs;
    private boolean                  hasCyclicDependencies;
    private GraphRenderer            renderer;
    private LibraryAnalysisExtension extension;

    @Override
    public void startProject(Project project) {
        super.startProject(project);
        hasConfigs = false;
        hasCyclicDependencies = false;
    }

    @Override
    public void completeProject(Project project) {
        if (!hasConfigs) {
            getTextOutput().withStyle(Info).println("No dependencies");
        }
        super.completeProject(project);
    }

    public void setExtension(LibraryAnalysisExtension extension) {
        this.extension = extension;
    }

    public void startVariant(final BaseVariantData variantData) {
        if (hasConfigs) {
            getTextOutput().println();
        }
        hasConfigs = true;
        renderer = new GraphRenderer(getTextOutput());
        renderer.visit(new Action<StyledTextOutput>() {
            @Override
            public void execute(StyledTextOutput styledTextOutput) {
                getTextOutput().withStyle(Identifier).text(variantData.getVariantConfiguration().getFullName());

                List<String> ignore = extension.getIgnore();
                if (ignore != null && !ignore.isEmpty()) {
                    for (String dependency : ignore) {
                        getTextOutput().println();
                        getTextOutput().withStyle(Normal).text("ignore - ");
                        getTextOutput().withStyle(Description).text(dependency);
                    }
                } else {
                    getTextOutput().println();
                    getTextOutput().withStyle(Description).text("no ignore");
                }

            }
        }, true);
    }

    public void render(final Library library) throws IOException {
        if (library == null || library.isEmpty()) {
            getTextOutput().withStyle(Info).text("No dependencies");
            getTextOutput().println();
            return;
        }
        // 总依赖库合并大小
        getTextOutput().withStyle(Info).text("Dependencies size: " + FileUtils.convertFileSize(library.getSize()));
        getTextOutput().println();

        renderChildren(library);
    }

    @Override
    public void complete() {
        if (hasCyclicDependencies) {
            getTextOutput().withStyle(Info).println(
                    "\n(*) - dependencies omitted (listed previously)");
        }

        super.complete();
    }

    private void renderChildren(@NonNull Library library) {
        renderer.startChildren();

        List<JarDependency> localJars = library.getJarDependencies();
        List<Library> libraries = library.getLibraries();

        if (localJars != null) {
            final boolean emptyChildren = libraries.isEmpty();
            final int count = localJars.size();

            int i = 0;
            for (JarDependency jarDependency : localJars) {
                renderJarDependency(jarDependency, emptyChildren && i == count - 1);
                i++;
            }
        }

        final int count = libraries.size();
        for (int i = 0; i < count; i++) {
            Library lib = libraries.get(i);
            renderLibraryDependency(lib, i == count - 1);
        }
        renderer.completeChildren();
    }


    private void renderLibraryDependency(final Library lib, boolean lastChild) {
        renderer.visit(new Action<StyledTextOutput>() {
            @Override
            public void execute(StyledTextOutput styledTextOutput) {
                LibraryDependency dependency = lib.getLibraryDependency();
                String size = " unknown file";
                boolean isLarge = false;
                File bundle = dependency.getBundle();
                if (bundle != null && bundle.exists()) {
                    long length = bundle.length();
                    isLarge = length >= extension.getSizeLimit();
                    size = "@" + FileUtils.getFileType(bundle.getName()) + "(" + FileUtils.convertFileSize(length) +
                            ")";
                }
                String total = lib.isIgnore() ? "ignore" : FileUtils.convertFileSize(lib.getSize());
                getTextOutput().style(Info).text("[" + total + "]");
                getTextOutput().style(Normal).text(" " + dependency.getName());
                getTextOutput().style(isLarge ? Failure : Identifier).text(size);
            }
        }, lastChild);

        renderChildren(lib);
    }

    private void renderJarDependency(final JarDependency jar, boolean lastChild) {
        renderer.visit(new Action<StyledTextOutput>() {
            @Override
            public void execute(StyledTextOutput styledTextOutput) {
                String size = " unknown file";
                boolean isLarge = false;
                File bundle = jar.getJarFile();
                if (bundle != null && bundle.exists()) {
                    long length = bundle.length();
                    isLarge = length > extension.getSizeLimit();
                    size = "(" + FileUtils.convertFileSize(length) + ")";
                }
                getTextOutput().style(isLarge ? Failure : Identifier).text("LOCAL: " + jar.getJarFile().getName() + size);
            }
        }, lastChild);
    }

}
