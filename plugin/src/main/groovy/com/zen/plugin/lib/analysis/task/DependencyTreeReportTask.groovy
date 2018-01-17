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

import java.util.regex.Pattern
import java.util.zip.ZipFile

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

        try {
            outputHtml()

            if (extension.showTree) {
                renderer.startConfiguration(configuration)
                renderer.render(configuration)
                renderer.completeConfiguration(configuration)
            }
        } catch (Exception e) {
            Logger.W.log("generate report file failed!!! ERROR: " + e.message)
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
        def dictionary = new FileDictionary(configuration.getIncoming().getFiles())

//        root.supplyInfo(extension, dictionary, packageChecker)
        def rootLib = Library.create(dep, dictionary)

        timer.mark(Logger.W, "create root library")

        extension.ignore?.each {
            rootLib.applyIgnoreLibrary(it)
        }

        def root = NodeConvert.convert(rootLib,
                NodeConvert.Args.get(dictionary).extension(extension).checker(packageChecker).brief(!extension.fullTree))

        timer.mark(Logger.W, "create root node")

        def msg = packageChecker.outputPackageRepeatList()
        def list = outputModuleList(rootLib, packageChecker)
        list.modules.each {
            Logger.D?.log("module: ${it.name}")
        }

        timer.mark(Logger.W, "output module list")

        printFiles(rootLib, output)

        timer.mark(Logger.W, "output file_info")

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

    static final String[] IGNORES = [
            "classes.jar", "R.txt", "AndroidManifest.xml", "annotations.zip", "META-INF/MANIFEST.MF",
            "proguard.txt", "aapt/AndroidManifest.xml"
    ]

    static final Pattern IGNORE_REGEX = Pattern.compile("res/values([^/]*)/([^.]+).xml")

    static class RepeatFileInfo {
        String id
        long fileSize

        RepeatFileInfo(String id, long fileSize) {
            this.id = id
            this.fileSize = fileSize
        }
    }

    static void printFiles(Library root, String output) {
        Map<String, Set<RepeatFileInfo>> fileMap = new HashMap<>()
        List<String> repeatFiles = new ArrayList<>()

        root.contains?.each {
            lib ->
                def aar = lib.file.file
                if (aar == null || !aar.exists()) {
                    return
                }

                def path = aar.absolutePath
                ZipFile zipFile = new ZipFile(path)
                zipFile.entries().each {
                    entry ->
                        if (entry.isDirectory()) {
                            return
                        }
                        if (IGNORES.contains(entry.name)
                                || IGNORE_REGEX.matcher(entry.name).find()) {
                            return
                        }

                        Set<RepeatFileInfo> collect = fileMap.get(entry.name)
                        if (collect == null) {
                            collect = new HashSet<>()
                            fileMap.put(entry.name, collect)
                        } else if (!repeatFiles.contains(entry.name)) {
                            repeatFiles.add(entry.name)
                        }
                        collect.add(new RepeatFileInfo(lib.id, entry.size))
                }
        }

        repeatFiles.sort(new Comparator<String>() {
            @Override
            int compare(String s, String t1) {
                return s <=> t1
            }
        })

        StringBuilder builder = new StringBuilder()
        repeatFiles.each {
            Set<RepeatFileInfo> fileInfos = fileMap.get(it)
            if (fileInfos != null) {
                builder.append(it).append("\r\n")
                fileInfos.each {
                    info ->
                        builder.append("\t").append(info.fileSize).append("\t").append(info.id).append("\r\n")
                }
            } else {
                builder.append("WARN: Not found librarys for ${it}. Is it a repeat file? \r\n")
            }
        }
        new File(output, "repeat_files.txt").setText(builder.toString(), "UTF-8")
    }

    static OutputModuleList outputModuleList(Library root, PackageChecker checker) {
        OutputModuleList list = new OutputModuleList()
        root.contains?.each {
            if (!it.file) {
                list.addModule(new OutputModuleList.DependencyOutput(it.id, 0, "",
                        "pom", "",
                        it.contains.size(), it.useCount, it.useCountImmediate, ""))
                return
            }
            def pkgName = checker.parseModuleName(it.id, it.file.file)
            def isRepeat = checker.isRepeatPackage(pkgName)
            list.addModule(new OutputModuleList.DependencyOutput(it.id, it.file.size, pkgName,
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
                list.addModule(new OutputModuleList.DependencyOutput(key, value.size, pkgName,
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