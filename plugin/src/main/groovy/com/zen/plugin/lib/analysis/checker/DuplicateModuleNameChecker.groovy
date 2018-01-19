package com.zen.plugin.lib.analysis.checker

import com.zen.plugin.lib.analysis.model.Library
import com.zen.plugin.lib.analysis.util.Logger
import org.apache.commons.io.IOUtils

import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * @author: zen. date: 2018/1/10 0010
 */
class DuplicateModuleNameChecker extends ErrorChecker implements ModuleParser {

    private static final Pattern PATTERN_PACKAGE = Pattern.compile("package=\"([^\"]+)\"")

    private final Map<String, String> moduleNames = new HashMap<>()
    private final Map<String, String> repeatModules = new HashMap<>()

    @Override
    boolean isTarget(Library lib, File zip) {
        return zip.name.endsWith(".aar") || !moduleNames.containsKey(lib.id)
    }

    @Override
    void onAnalysisFile(Library lib, ZipFile file, ZipEntry entry) {
        if (entry.name != "AndroidManifest.xml") {
            return
        }

        def name = null
        boolean isFound = false

        if (entry) {
            def content = IOUtils.readLines(file.getInputStream(entry), "UTF-8")
            isFound = content.find {
                Matcher m = PATTERN_PACKAGE.matcher(it)
                boolean isMatch = m.find()
                if (isMatch) {
                    name = m.group(1)
                }
                isMatch
            }
        }

        if (!entry) {
            name = "Not Found Manifest.xml"
        } else if (!isFound) {
            name = "Not Found Package in AndroidManifest.xml"
        } else {
            putModuleIfRepeat(name, lib.id)
        }
    }

    @Override
    String outputReport() {
        return outputPackageRepeatList()
    }

    boolean isRepeatPackage(String pkgName) {
        return repeatModules.get(pkgName)
    }

    @Override
    String parseModuleName(String module, File aar) {
        return moduleNames.get(module)
    }

    private void putModuleIfRepeat(String packageName, String module) {
        Map.Entry<String, String> result = moduleNames.find {
            key, value ->
                (value == packageName)
        }
        if (result) {
            if (repeatModules.containsKey(packageName)) {
                def value = repeatModules.get(packageName)
                Logger.D?.log("repeat: ${value}")
                value += ",${module}"
                Logger.D?.log("repeat add to: ${value}")
                repeatModules.put(packageName, value)
            } else {
                repeatModules.put(packageName, "${result.key},${module}")
            }
        }
    }

    String outputPackageRepeatList() {
        def builder = new StringBuilder()
        if (!repeatModules.isEmpty()) {
            def enter = "\r\n"
            builder.append("## Repeat Module Name").append(enter).append(enter)
            repeatModules.each {
                key, value ->
                    builder.append('*').append(key).append(" in ").append(value).append(enter)
            }
        }
        builder.toString()
    }
}
