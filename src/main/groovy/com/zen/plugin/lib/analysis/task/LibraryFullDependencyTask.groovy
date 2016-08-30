package com.zen.plugin.lib.analysis.task

import com.zen.plugin.lib.analysis.VariantAnalysisHelper
import com.zen.plugin.lib.analysis.conf.LibraryAnalysisExtension

//import com.android.build.gradle.internal.tasks.DependencyReportTask;
import com.zen.plugin.lib.analysis.model.FileWrapper
import com.zen.plugin.lib.analysis.model.Node
import com.zen.plugin.lib.analysis.renderer.LibraryGraphHtmlReportRenderer
import com.zen.plugin.lib.analysis.renderer.LibraryHtmlReportRenderer
import com.zen.plugin.lib.analysis.renderer.LibraryMdReportRenderer
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.result.ResolutionResult
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

    private static void copyResources(String targetPath) {
        def files = [
                "css/z/img/",
                "css/demo.css",
                "css/z/ztree.css",
                "css/z/img/line_conn.gif",
                "css/z/img/loading.gif",
                "css/z/img/zTreeStandard.gif",
                "css/z/img/zTreeStandard.png",

                "js/",
                "js/jquery.ztree.core.min.js",
                "js/jquery-1.4.4.min.js",
                "js/cytoscape-dagre.js",
                "js/dagre.min.js"
        ]

        files.each {
            if (it.endsWith('/')) {
                new File(targetPath, it).mkdirs()
                return
            }
            def source = getClass().getResourceAsStream("/com/zen/plugin/lib/analysis/${it}")
            def target = new File(targetPath, it)
            target.withDataOutputStream {
                os -> os << source
            }
        }
    }

    public void generate(Project project) throws IOException {
        SortedSet<Configuration> sortedConfigurations = new TreeSet<Configuration>(new Comparator<Configuration>() {
            public int compare(Configuration conf1, Configuration conf2) {
                return conf1.getName().compareTo(conf2.getName());
            }
        });
        sortedConfigurations.addAll(getReportConfigurations());

        for (Configuration configuration : sortedConfigurations) {
            renderer.startConfiguration(configuration);
            renderer.render(configuration);
            renderer.completeConfiguration(configuration);
            renderNodeTree(configuration)
        }
    }

    private void renderNodeTree(Configuration configuration) {
        ResolutionResult result = configuration.getIncoming().getResolutionResult()
        Node root = VariantAnalysisHelper.convertDependencyNode(result)

        copyResources(prepareTargetPath(configuration))

        LibraryHtmlReportRenderer renderer = new LibraryHtmlReportRenderer();
        renderer.setOutputFile(prepareOutputFile(configuration, "TreeDependencies.html"));
        renderer.render(root);

        LibraryGraphHtmlReportRenderer graphRenderer = new LibraryGraphHtmlReportRenderer();
        graphRenderer.setOutputFile(prepareOutputFile(configuration, "GraphDependencies.html"));
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
        getProject().getConfigurations();
    }

    private String prepareTargetPath(Configuration configuration) {
        def path = "${project.buildDir}/" + extension.outputPath + "/${configuration.name}"
        File file = new File(path)
        if (!file.exists()) {
            file.mkdirs()
        }
        path
    }

    private File prepareOutputFile(Configuration configuration, String fileName) {
        def path = prepareTargetPath(configuration)
        File analysisFile = new File(path, fileName)
        if (analysisFile.exists()) {
            analysisFile.delete()
        }
        analysisFile.createNewFile()
        analysisFile
    }

}
