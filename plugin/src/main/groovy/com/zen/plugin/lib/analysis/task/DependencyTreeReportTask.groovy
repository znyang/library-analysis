package com.zen.plugin.lib.analysis.task

import com.zen.plugin.lib.analysis.convert.NodeConvert
import com.zen.plugin.lib.analysis.ext.LibraryAnalysisExtension
import com.zen.plugin.lib.analysis.model.FileDictionary
import com.zen.plugin.lib.analysis.model.Library
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
        def timer = new Timer()

        outputHtml()

        if (extension.showTree) {
            renderer.startConfiguration(configuration)
            renderer.render(configuration)
            renderer.completeConfiguration(configuration)
        }

        timer.mark(Logger.W, "${getName()} total")
    }

    private void outputHtml() {
        def timer = new Timer()

        def output = prepareOutputPath()
        ResourceUtils.copyResources(output)

        timer.mark(Logger.W, "copy resources")

        def resolutionResult = configuration.getIncoming().getResolutionResult()
        def dep = new RenderableModuleResult(resolutionResult.getRoot())

        timer.mark(Logger.W, "get dependencies")

//        def root = Node.create(dep)

//        timer.mark(Logger.W, "create nodes")

        // 通过依赖文件创建依赖字典
        def packageChecker = new PackageChecker()

        timer.mark(Logger.W, "create checker")

        def dictionary = new FileDictionary(configuration.getIncoming().getFiles())

        timer.mark(Logger.W, "create dictionary")

//        root.supplyInfo(extension, dictionary, packageChecker)
        def rootLib = Library.create(dep, dictionary)

        timer.mark(Logger.W, "create root library")

        extension.ignore?.each {
            rootLib.applyIgnoreLibrary(it)
        }

        timer.mark(Logger.W, "apply ignore")

        def root = NodeConvert.convert(rootLib,
                NodeConvert.Args.get(dictionary).extension(extension).checker(packageChecker).brief(!extension.fullTree))

        timer.mark(Logger.W, "create root node")

        def msg = packageChecker.outputPackageRepeatList()
        def list = outputModuleList(rootLib, packageChecker)
        list.modules.each {
            Logger.D?.log("module: ${it.name}")
        }

        timer.mark(Logger.W, "output module list")

        if (extension.output.contains("html")) {
            def result = new HtmlRenderer(output).render(root, list, msg)
            if (msg && !msg.isEmpty()) {
                println msg
            }
            Logger.W?.log("Html output: ${result}")

            timer.mark(Logger.W, "output html file")
        }

        if (extension.output.contains("txt")) {
            def result = new TextRenderer(output).render(root, list, msg)
            Logger.W?.log("Txt output: ${result}")

            timer.mark(Logger.W, "output txt file")
        }
    }

    static OutputModuleList outputModuleList(Library root, PackageChecker checker) {
        OutputModuleList list = new OutputModuleList()
        root.contains?.each {
            if (!it.file) {
                list.addModule(new OutputModuleList.DependencyOutput(it.id, 0, "Not Found File!",
                        "???", "",
                        it.contains.size(), it.useCount, it.useCountImmediate, "danger"))
                return
            }
            def pkgName = checker.parseModuleName(it.id, it.file.file)
            def isRepeat = checker.isRepeatPackage(pkgName)
            list.addModule(new OutputModuleList.DependencyOutput(it.id, it.getTotalSizeWithoutIgnore(), it.file.size, pkgName,
                    it.file.type, isRepeat ? "package name repeat" : "",
                    it.contains.size(), it.useCount, it.useCountImmediate, isRepeat ? "danger" : ""))
        }
        list.sortModules()
        list
    }

    @Deprecated
    static OutputModuleList outputModuleList(FileDictionary dictionary, PackageChecker checker) {
        OutputModuleList list = new OutputModuleList()
        dictionary.cacheInfoMap.each {
            key, value ->
                def pkgName = checker.parseModuleName(key, value.file)
                def isRepeat = checker.isRepeatPackage(pkgName)
                list.addModule(new OutputModuleList.DependencyOutput(key, 0, value.size, pkgName,
                        value.type, isRepeat ? "package name repeat" : "", 0, 0, 0, isRepeat ? "danger" : ""))
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