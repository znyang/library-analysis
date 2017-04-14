package com.zen.plugin.lib.analysis.task

import com.zen.plugin.lib.analysis.ext.LibraryAnalysisExtension
import com.zen.plugin.lib.analysis.model.DependencyDictionary
import com.zen.plugin.lib.analysis.model.Node
import com.zen.plugin.lib.analysis.render.HtmlRenderer
import com.zen.plugin.lib.analysis.render.OutputModuleList
import com.zen.plugin.lib.analysis.render.TextRenderer
import com.zen.plugin.lib.analysis.util.Logger
import com.zen.plugin.lib.analysis.util.PackageChecker
import com.zen.plugin.lib.analysis.util.ResourceUtils
import com.zen.plugin.lib.analysis.util.Timer
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.diagnostics.AbstractReportTask
import org.gradle.api.tasks.diagnostics.internal.ReportRenderer
import org.gradle.api.tasks.diagnostics.internal.dependencies.AsciiDependencyReportRenderer
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableModuleResult

/**
 * @author zen
 * @version 2016/9/9
 */
class DependencyTreeReportTask extends AbstractReportTask {
    def renderer = new AsciiDependencyReportRenderer()

    Configuration configuration
    LibraryAnalysisExtension extension

    @Override
    protected ReportRenderer getRenderer() {
        return renderer
    }

    @Override
    protected void generate(Project project) throws IOException {
        outputHtml()

        if (extension.showTree) {
            renderer.startConfiguration(configuration)
            renderer.render(configuration)
            renderer.completeConfiguration(configuration)
        }
    }

    private void outputHtml() {
        def output = prepareOutputPath()
        ResourceUtils.copyResources(output)

        def timer = new Timer()

        def resolutionResult = configuration.getIncoming().getResolutionResult()
        def dep = new RenderableModuleResult(resolutionResult.getRoot())
        def root = Node.create(dep)

        timer.mark(Logger.W, "create nodes")

        // 通过依赖文件创建依赖字典
        def packageChecker = new PackageChecker()
        def dictionary = new DependencyDictionary(configuration.getIncoming().getFiles())
        root.supplyInfo(extension, dictionary, packageChecker)

        timer.mark(Logger.W, "supply info")

        def msg = packageChecker.outputPackageRepeatList()
        def list = outputModuleList(dictionary, packageChecker)
        list.modules.each {
            Logger.D?.log("module: ${it.name}")
        }

        def result = new HtmlRenderer(output).render(root, list, msg)
        if (msg && !msg.isEmpty()) {
            println msg
        }
        println "output result: ${result}"

        new TextRenderer(output).render(root, list, msg)
    }

    static OutputModuleList outputModuleList(DependencyDictionary dictionary, PackageChecker checker) {
        OutputModuleList list = new OutputModuleList()
        dictionary.cacheInfoMap.each {
            key, value ->
                def pkgName = checker.parseModuleName(key, value.file)
                def isRepeat = checker.isRepeatPackage(pkgName)
                list.addModule(new OutputModuleList.DependencyOutput(key, value.size, pkgName,
                            value.type, isRepeat ? "package name repeat" : "", isRepeat ? "danger" : ""))
        }
        list.sortModules()
        list
    }

    private String prepareOutputPath() {
        def path = "${project.buildDir}/${extension.outputPath}/${configuration.name}"
        def file = new File(path)
        if (!file.exists()) {
            file.mkdirs()
        }
        path
    }

}