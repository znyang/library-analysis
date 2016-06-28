package com.zen.plugin.lib.analysis.task

import com.android.build.gradle.internal.variant.BaseVariantData
import com.zen.plugin.lib.analysis.VariantAnalysisHelper
import com.zen.plugin.lib.analysis.conf.LibraryAnalysisExtension
import com.zen.plugin.lib.analysis.model.Library
import com.zen.plugin.lib.analysis.renderer.LibraryAnalysisReportRenderer
import com.zen.plugin.lib.analysis.renderer.LibraryCsvReportRenderer
import com.zen.plugin.lib.analysis.renderer.LibraryMdReportRenderer
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.logging.StyledTextOutputFactory

class LibraryDependencyReportTask extends DefaultTask {

    private static final String FILE_LIBRARY_ANALYSIS = "LibraryAnalysis.txt";
    private static final String FILE_LARGE_FILE_REPORT = "LargeFiles.md";
    private static final String FILE_DEPENDENCY_RANKING = "LibraryRanking.md";
    private static final String FILE_DEPENDENCY_STATISTICS_RANKING = "LibraryStatisticsRanking.md";
    private static final String FILE_DEPENDENCY_CSV = "LibraryStatistics.csv";

    private BaseVariantData variant;
    private LibraryAnalysisExtension extension;

    @TaskAction
    public void generate() throws IOException {
        if (variant == null) {
            return
        }

        Library library = VariantAnalysisHelper.analysis(variant, extension.ignore, extension.limit.getFileSize());
        renderConsole(library)
        renderReportFile(library);
        renderLargeReportFile(library);
        renderRankingReportFile(library);
//        renderStatisticsRankingReportFile(library);
        renderStatisticsCsvReportFile(library);
    }

    private void renderConsole(Library library) {
        LibraryAnalysisReportRenderer renderer = new LibraryAnalysisReportRenderer();
        renderer.setOutput(getServices().get(StyledTextOutputFactory.class).create(getClass()));
        renderer.setExtension(extension);
        renderer.startVariant(variant);
        renderer.render(library);
    }

    private void renderReportFile(Library library) {
        LibraryAnalysisReportRenderer renderer = new LibraryAnalysisReportRenderer();
        renderer.setExtension(extension);
        renderer.setOutputFile(prepareOutputFile(FILE_LIBRARY_ANALYSIS));
        renderer.startVariant(variant);
        renderer.render(library);
    }

    private void renderLargeReportFile(Library library) {
        LibraryMdReportRenderer renderer = new LibraryMdReportRenderer();
        renderer.setOutputFile(prepareOutputFile(FILE_LARGE_FILE_REPORT));
        renderer.render(library.findAllLargeFileWrapper());
    }

    private void renderRankingReportFile(Library library) {
        LibraryMdReportRenderer renderer = new LibraryMdReportRenderer();
        renderer.setOutputFile(prepareOutputFile(FILE_DEPENDENCY_RANKING));
        renderer.render(library.findAllDependencyAarWrapper());
    }

    private void renderStatisticsRankingReportFile(Library library) {
        LibraryMdReportRenderer renderer = new LibraryMdReportRenderer();
        renderer.setOutputFile(prepareOutputFile(FILE_DEPENDENCY_STATISTICS_RANKING));
        renderer.render(library.findAllDependencySizeWrapper());
    }

    private void renderStatisticsCsvReportFile(Library library) {
        LibraryCsvReportRenderer renderer = new LibraryCsvReportRenderer();
        renderer.setOutputFile(prepareOutputFile(FILE_DEPENDENCY_CSV));
        renderer.render(library.findAllDependencyWrapper());
    }

    private File prepareOutputFile(String fileName) {
        def path = "${project.buildDir}/" + extension.outputPath + "/${variant.name}"
        new File(path).mkdirs();

        File analysisFile = new File(path, fileName);
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
