package com.zen.plugin.lib.analysis.util

import org.apache.commons.io.IOUtils

import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipFile;

/**
 * PackageChecker
 *
 * @author znyang 2017/2/13 0013
 */

public class PackageChecker {

    private final Map<String, String> sModuleNames = new HashMap<>();
    private final Map<String, String> sModuleRepeat = new HashMap<>();
    private static final Pattern PATTERN_PACKAGE = Pattern.compile("package=\"([^\"]+)\"")

    public void initModuleInfo() {
        sModuleNames.clear();
        sModuleRepeat.clear();
    }

    public String parseModuleName(String module, File aar) {
        if (aar == null || !aar.name.endsWith(".aar") || !aar.exists()) {
            return ""
        }
        Logger.D?.log("package name parse ${aar.name}")

        def path = aar.absolutePath
        if (sModuleNames.containsKey(module)) {
            return sModuleNames.get(module)
        }

        ZipFile zipFile = new ZipFile(path)
        ZipEntry manifest = zipFile.entries().find {
            it.name.equals("AndroidManifest.xml")
        };
        def name = null
        boolean isFound = false
        if (manifest) {
            def content = IOUtils.readLines(zipFile.getInputStream(manifest), "UTF-8")
            isFound = content.find {
                Matcher m = PATTERN_PACKAGE.matcher(it)
                boolean isMatch = m.find()
                if (isMatch) {
                    name = m.group(1)
                }
                isMatch
            }
        }

        if (!manifest) {
            name = "Not Found Manifest.xml"
        } else if (!isFound) {
            name = "Not Found Package in AndroidManifest.xml"
        } else {
            putModuleIfRepeat(name, module)
        }

        sModuleNames.put(module, name)
        Logger.D?.log("package name find result: ${module} -> ${name}")
        return name
    }

    private void putModuleIfRepeat(String packageName, String module) {
        Map.Entry<String, String> result = sModuleNames.find {
            key, value ->
                value.equals(packageName)
        }
        if (result) {
            if (sModuleRepeat.containsKey(packageName)) {
                def value = sModuleRepeat.get(packageName)
                Logger.D?.log("repeat: ${value}")
                value += ",${module}"
                Logger.D?.log("repeat add to: ${value}")
                sModuleRepeat.put(packageName, value)
            } else {
                sModuleRepeat.put(packageName, "${result.key},${module}")
            }
        }
    }

    public boolean isRepeatPackage(String pkgName) {
        return sModuleRepeat.get(pkgName)
    }

    public String outputPackageRepeatList() {
        def builder = new StringBuilder();
        if (!sModuleRepeat.isEmpty()) {
            def enter = "\r\n";
            builder.append("WARNING: Package Name Repeat!!!").append(enter)
            sModuleRepeat.each {
                key, value ->
                    builder.append(key).append(" use in [${value}]").append(enter)
            }
        }
        builder.toString()
    }

}
