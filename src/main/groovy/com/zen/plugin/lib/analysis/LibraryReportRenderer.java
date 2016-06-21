package com.zen.plugin.lib.analysis;

import com.android.annotations.NonNull;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.builder.dependency.JarDependency;
import com.android.builder.dependency.LibraryDependency;
import com.zen.plugin.lib.analysis.model.AarFile;
import com.zen.plugin.lib.analysis.model.Library;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.tasks.diagnostics.internal.TextReportRenderer;
import org.gradle.internal.graph.GraphRenderer;
import org.gradle.logging.StyledTextOutput;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;

import static org.gradle.logging.StyledTextOutput.Style.Description;
import static org.gradle.logging.StyledTextOutput.Style.Error;
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

        renderChildren(library, null);
    }

    @Override
    public void complete() {
        if (hasCyclicDependencies) {
            getTextOutput().withStyle(Info).println(
                    "\n(*) - dependencies omitted (listed previously)");
        }

        super.complete();
    }

    private void renderChildren(@NonNull Library library, File parentBundle) {
        renderer.startChildren();


        List<JarDependency> localJars = library.getJarDependencies();
        List<Library> libraries = library.getLibraries();
        final boolean emptyChildren = libraries.isEmpty();
        final int count = localJars == null ? 0 : localJars.size();

        if (parentBundle != null) {
            renderAarLargeFiles(parentBundle, emptyChildren && count == 0);
        }

        if (count > 0) {
            int i = 0;
            for (JarDependency jarDependency : localJars) {
                renderJarDependency(jarDependency, emptyChildren && i == count - 1);
                i++;
            }
        }

        final int librarySize = libraries.size();
        for (int i = 0; i < librarySize; i++) {
            Library lib = libraries.get(i);
            renderLibraryDependency(lib, i == librarySize - 1);
        }
        renderer.completeChildren();
    }


    private void renderLibraryDependency(final Library lib, final boolean lastChild) {
        final List<File> bundleFiles = new ArrayList<>();
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
                    bundleFiles.add(bundle);
                }

                String total = lib.isIgnore() ? "ignore" : FileUtils.convertFileSize(lib.getSize());

                getTextOutput().style(Info).text("[" + total + "]");
                getTextOutput().style(Normal).text(" " + dependency.getName());
                getTextOutput().style(isLarge ? Failure : Identifier).text(size);
            }
        }, lastChild);

        renderChildren(lib, bundleFiles.isEmpty() ? null : bundleFiles.get(0));
    }

    private void renderAarLargeFiles(File bundle, boolean lastChild) {
        String type = FileUtils.getFileType(bundle.getName());
        if ("aar".equals(type)) {
            try {
                AarFile file = new AarFile(bundle);
                List<ZipEntry> entries = file.getEntries();
                List<ZipEntry> largeEntries = findLargeEntries(entries);

                if (!largeEntries.isEmpty()) {
                    final int size = largeEntries.size();
                    renderer.visit(new Action<StyledTextOutput>() {
                        @Override
                        public void execute(StyledTextOutput styledTextOutput) {
                            getTextOutput().style(Failure).text("Large Files: ");
                        }
                    }, lastChild);

                    renderer.startChildren();
                    for (int i = 0; i < size; i++) {
                        showZipEntry(largeEntries.get(i), i == size - 1);
                    }
                    renderer.completeChildren();
                }
            } catch (IOException e) {
                getTextOutput().style(Error).println(e);
            }
        }
    }

    private List<ZipEntry> findLargeEntries(List<ZipEntry> entries) {
        List<ZipEntry> largeEntries = new ArrayList<>();

        for (ZipEntry entry : entries) {
            if (entry.getName().endsWith(".jar")) {
                continue;
            }
            if (entry.getSize() >= extension.limit.getFileSize()) {
                largeEntries.add(entry);
            }
        }
        return largeEntries;
    }

    private void showZipEntry(final ZipEntry entry, boolean lastChild) {
        renderer.visit(new Action<StyledTextOutput>() {
            @Override
            public void execute(StyledTextOutput styledTextOutput) {
                getTextOutput().style(Failure).text(entry.getName() + " - "
                        + FileUtils.convertFileSize(entry.getSize()));
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
