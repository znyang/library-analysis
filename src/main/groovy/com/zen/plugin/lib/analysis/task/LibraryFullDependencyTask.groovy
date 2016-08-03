package com.zen.plugin.lib.analysis.task

import com.zen.plugin.lib.analysis.VariantAnalysisHelper
import com.zen.plugin.lib.analysis.conf.LibraryAnalysisExtension
import com.zen.plugin.lib.analysis.log.ILog;


//import com.android.build.gradle.internal.tasks.DependencyReportTask;
import com.zen.plugin.lib.analysis.log.LogReportRenderer
import com.zen.plugin.lib.analysis.log.Logger
import com.zen.plugin.lib.analysis.model.FileWrapper
import com.zen.plugin.lib.analysis.model.Node
import com.zen.plugin.lib.analysis.renderer.LibraryGraphHtmlReportRenderer
import com.zen.plugin.lib.analysis.renderer.LibraryHtmlReportRenderer
import com.zen.plugin.lib.analysis.renderer.LibraryMdReportRenderer
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.internal.tasks.options.Option
import org.gradle.api.tasks.diagnostics.AbstractReportTask
import org.gradle.api.tasks.diagnostics.internal.DependencyReportRenderer
import org.gradle.api.tasks.diagnostics.internal.ReportRenderer
import org.gradle.api.tasks.diagnostics.internal.dependencies.AsciiDependencyReportRenderer

public class LibraryFullDependencyTask extends AbstractReportTask {

    private DependencyReportRenderer renderer = new AsciiDependencyReportRenderer();

    private Set<Configuration> configurations;
    private LibraryAnalysisExtension extension;

    public ReportRenderer getRenderer() {
        return renderer;
    }

    /**
     * Set the renderer to use to build a report. If unset, AsciiGraphRenderer will be used.
     */
    public void setRenderer(DependencyReportRenderer renderer) {
        this.renderer = renderer;
    }

    public void generate(Project project) throws IOException {
        SortedSet<Configuration> sortedConfigurations = new TreeSet<Configuration>(new Comparator<Configuration>() {
            public int compare(Configuration conf1, Configuration conf2) {
                return conf1.getName().compareTo(conf2.getName());
            }
        });
        sortedConfigurations.addAll(getReportConfigurations());

        ILog logger = new Logger();
        for (Configuration configuration : sortedConfigurations) {
            renderer.startConfiguration(configuration);
            renderer.render(configuration);
            renderer.completeConfiguration(configuration);

//            SortedSet<FileWrapper> wrappers = VariantAnalysisHelper.analysis(project, configuration, logger)
//            renderAllJarFiles(wrappers)

            renderNodeTree(configuration)
        }

        new LogReportRenderer("FullDependencyTask.log", "${project.buildDir}/" + extension.outputPath).renderLog(logger);
    }

    private void renderNodeTree(Configuration configuration) {
        // copy resource files
        // FIXME: not working
//        ["js/jquery.ztree.core.min.js", "js/jquery-1.4.4.min.js",
//         "css/demo.css", "css/zTreeStyle/zTreeStyle.css",
//         "css/zTreeStyle/line_conn.gif", "css/zTreeStyle/loading.gif", "css/zTreeStyle/zTreeStandard.gif", "css/zTreeStyle/zTreeStandard.png"]
//                .each { String resourceName ->
//            def resource = getClass().getResourceAsStream("/com/zen/plugin/lib/analysis/" + resourceName);
//            def targetFile = new File("${project.buildDir}/" + extension.outputPath + "/${configurations.name}", resourceName)
//            targetFile.mkdirs()
//            targetFile.write resource.text
//        }
        Node root = VariantAnalysisHelper.convertDependencyNode(configuration.getIncoming().getResolutionResult());

        LibraryHtmlReportRenderer renderer = new LibraryHtmlReportRenderer();
        renderer.setOutputFile(prepareOutputFile("TreeDependencies.html"));
        renderer.render(root);

        LibraryGraphHtmlReportRenderer graphRenderer = new LibraryGraphHtmlReportRenderer();
        graphRenderer.setOutputFile(prepareOutputFile("GraphDependencies.html"));
        graphRenderer.render(root);
    }

    private void renderAllJarFiles(SortedSet<FileWrapper> wrappers) {
        LibraryMdReportRenderer renderer = new LibraryMdReportRenderer()
        renderer.setOutputFile(prepareOutputFile("SortAllJarFiles.md"))
        renderer.renderOnlyFileName(wrappers)
    }

    public LibraryAnalysisExtension getExtension() {
        return extension;
    }

    public void setExtension(LibraryAnalysisExtension extension) {
        this.extension = extension;
    }

    private Set<Configuration> getReportConfigurations() {
        return configurations != null ? configurations : getTaskConfigurations();
    }

    /**
     * Returns the configurations to generate the report for. Defaults to all configurations of this task's containing
     * project.
     *
     * @return the configurations.
     */
    public Set<Configuration> getConfigurations() {
        return configurations;
    }

    /**
     * Sets the configurations to generate the report for.
     *
     * @param configurations The configuration. Must not be null.
     */
    public void setConfigurations(Set<Configuration> configurations) {
        this.configurations = configurations;
    }

    /**
     * Sets the single configuration (by name) to generate the report for.
     *
     * @param configurationName name of the configuration to generate the report for
     */
    @Option(option = "configuration", description = "The configuration to generate the report for.")
    public void setConfiguration(String configurationName) {
        this.configurations = Collections.singleton(getTaskConfigurations().getByName(configurationName));
    }

    public ConfigurationContainer getTaskConfigurations() {
        return getProject().getConfigurations();
    }

    private File prepareOutputFile(String fileName) {
        def path = "${project.buildDir}/" + extension.outputPath + "/${configurations.name}"
        new File(path).mkdirs();

        File analysisFile = new File(path, fileName);
        if (analysisFile.exists()) {
            analysisFile.delete()
        }
        analysisFile.createNewFile()
        analysisFile
    }

}
