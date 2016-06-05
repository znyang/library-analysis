package com.zen.plugin.lib.analysis

import com.android.build.gradle.internal.variant.BaseVariantData
import com.zen.plugin.lib.analysis.model.Library
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.logging.StyledTextOutputFactory

class LibraryDependencyReportTask extends DefaultTask {

    private BaseVariantData variant;
    private LibraryAnalysisExtension extension;

    @TaskAction
    public void generate() throws IOException {
        if (variant == null) {
            return
        }

        Library library = new VariantAnalysisHelper().analysis(variant, extension.ignore);
        renderConsole(library)
        renderReportFile(library);
    }

    private void renderConsole(Library library) {
        LibraryReportRenderer renderer = new LibraryReportRenderer();
        renderer.setOutput(getServices().get(StyledTextOutputFactory.class).create(getClass()));
        renderer.setExtension(extension);
        renderer.startVariant(variant);
        renderer.render(library);
    }

    private void renderReportFile(Library library) {
        LibraryReportRenderer renderer = new LibraryReportRenderer();
        renderer.setExtension(extension);
        renderer.setOutputFile(prepareOutputFile());
        renderer.startVariant(variant);
        renderer.render(library);
    }

    private File prepareOutputFile() {
        def path = "${project.buildDir}/" + extension.outputPath + "/${variant.name}"
        new File(path).mkdirs();

        File analysisFile = new File(path, "libraryAnalysis.txt");
        if (analysisFile.exists()) {
            analysisFile.delete();
        }
        analysisFile.createNewFile();
        analysisFile
    }

    BaseVariantData getVariant() {
        return variant
    }

    void setVariant(BaseVariantData variant) {
        this.variant = variant
    }

    LibraryAnalysisExtension getExtension() {
        return extension
    }

    void setExtension(LibraryAnalysisExtension extension) {
        this.extension = extension
    }

}
