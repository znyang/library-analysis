package com.zen.plugin.lib.analysis.task

import com.zen.plugin.lib.analysis.ext.LibraryAnalysisExtension
import com.zen.plugin.lib.analysis.model.DependencyDictionary
import com.zen.plugin.lib.analysis.model.Node
import com.zen.plugin.lib.analysis.render.HtmlRenderer
import com.zen.plugin.lib.analysis.util.Logger
import com.zen.plugin.lib.analysis.util.ResourceUtils
import com.zen.plugin.lib.analysis.util.Timer
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.result.ResolutionResult
import org.gradle.api.tasks.diagnostics.AbstractReportTask
import org.gradle.api.tasks.diagnostics.internal.ReportRenderer
import org.gradle.api.tasks.diagnostics.internal.dependencies.AsciiDependencyReportRenderer
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableDependency
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableModuleResult

/**
 * @author zen
 * @version 2016/9/9
 */
class DependencyTreeReportTask extends AbstractReportTask {
    def renderer = new AsciiDependencyReportRenderer()

    Configuration            configuration
    LibraryAnalysisExtension extension
    Logger                   log

    @Override
    protected ReportRenderer getRenderer() {
        return renderer
    }

    @Override
    protected void generate(Project project) throws IOException {
        String output = prepareOutputPath()
        ResourceUtils.copyResources(output)

        def timer = new Timer()

        ResolutionResult resolutionResult = configuration.getIncoming().getResolutionResult()
        RenderableDependency dep = new RenderableModuleResult(resolutionResult.getRoot())
        Node root = Node.create(dep)

        timer.mark(log, "create nodes")

        DependencyDictionary dictionary = new DependencyDictionary(configuration.getIncoming().getFiles())
        root.supplyInfo(extension, dictionary)

        timer.mark(log, "supply info")

        new HtmlRenderer(output).render(root)

        if (extension.showTree) {
            renderer.startConfiguration(configuration)
            renderer.render(configuration)
            renderer.completeConfiguration(configuration)
        }
    }

    private String prepareOutputPath() {
        def path = "${project.buildDir}/${extension.outputPath}/${configuration.name}"
        File file = new File(path)
        if (!file.exists()) {
            file.mkdirs()
        }
        path
    }

}