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
import java.util.zip.ZipEntry;

import static org.gradle.logging.StyledTextOutput.Style.Description;
import static org.gradle.logging.StyledTextOutput.Style.Failure;
import static org.gradle.logging.StyledTextOutput.Style.Identifier;
import static org.gradle.logging.StyledTextOutput.Style.Info;
import static org.gradle.logging.StyledTextOutput.Style.Normal;

/**
 * @author zen
 * @version 2016/6/4
 */
public class LibraryAnalysisReportRenderer extends TextReportRenderer {

    private boolean hasConfigs;
    private boolean hasCyclicDependencies;
    private GraphRenderer renderer;
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
        final boolean emptyChildren = libraries.isEmpty();
        final int count = localJars == null ? 0 : localJars.size();

        renderAarLargeFiles(library.getLargeEntries(), emptyChildren && count == 0);

        if (count > 0) {
            int i = 0;
            for (JarDependency jarDependency : localJars) {
                renderJarDependency(jarDependency, emptyChildren && i == count - 1);
                i++;
            }
        }

        final int librarySize = libraries.size();
        final String mark = extension.getMark();
        for (int i = 0; i < librarySize; i++) {
            Library lib = libraries.get(i);
            renderLibraryDependency(lib, mark, i == librarySize - 1);
        }
        renderer.completeChildren();
    }


    private void renderLibraryDependency(final Library lib, final String mark, final boolean lastChild) {
        renderer.visit(new Action<StyledTextOutput>() {
            @Override
            public void execute(StyledTextOutput styledTextOutput) {
                String size = " unknown file";
                boolean isLarge = false;

                LibraryDependency dependency = lib.getLibraryDependency();
                File bundle = dependency.getBundle();
                String type = null;

                if (bundle != null && bundle.exists()) {
                    long length = bundle.length();
                    type = FileUtils.getFileType(bundle.getName());
                    isLarge = length >= extension.limit.getLibSize();
                    size = "@" + type + "(" + FileUtils.convertFileSize(length) +
                            ")";
                }

                String total = lib.isIgnore() ? "ignore" : FileUtils.convertFileSize(lib.getSize());

                getTextOutput().style(Info).text("[" + total + "]");
                getTextOutput().style(Normal).text(" " + dependency.getName());
                getTextOutput().style(isLarge ? Failure : Identifier).text(size);
                if (isLarge && !lib.isIgnore()) {
                    getTextOutput().style(Failure).text(mark);
                }
            }
        }, lastChild);

        renderChildren(lib);
    }

    private void renderAarLargeFiles(@NonNull List<ZipEntry> files, boolean lastChild) {
        if (files.isEmpty()) {
            return;
        }

        final int size = files.size();
        renderer.visit(new Action<StyledTextOutput>() {
            @Override
            public void execute(StyledTextOutput styledTextOutput) {
                getTextOutput().style(Failure).text("Large Files: ");
            }
        }, lastChild);

        renderer.startChildren();
        String mark = extension.getMark();
        for (int i = 0; i < size; i++) {
            showZipEntry(files.get(i), mark, i == size - 1);
        }
        renderer.completeChildren();
    }

    private void showZipEntry(final ZipEntry entry, final String mark, boolean lastChild) {
        renderer.visit(new Action<StyledTextOutput>() {
            @Override
            public void execute(StyledTextOutput styledTextOutput) {
                getTextOutput().style(Failure).text(entry.getName() + " - "
                        + FileUtils.convertFileSize(entry.getSize()));
                getTextOutput().style(Failure).text(mark);
            }
        }, lastChild);
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
                    isLarge = length > extension.limit.getLibSize();
                    size = "(" + FileUtils.convertFileSize(length) + ")";
                }
                getTextOutput().style(isLarge ? Failure : Identifier).text("LOCAL: " + jar.getJarFile().getName() + size);
            }
        }, lastChild);
    }

}
