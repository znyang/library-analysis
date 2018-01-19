package com.zen.plugin.lib.analysis.checker

import com.zen.plugin.lib.analysis.model.Library
import com.zen.plugin.lib.analysis.util.FileUtils

import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * 重复文件检查（包括class）
 *
 * @author: zen. date: 2018/1/10 0010
 */
class DuplicateFilesChecker extends ErrorChecker {

    static final String[] IGNORES = [
            "classes.jar", "R.txt", "AndroidManifest.xml", "annotations.zip", "META-INF/MANIFEST.MF",
            "proguard.txt", "aapt/AndroidManifest.xml", "public.txt", "public.xml"
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

    Map<String, Set<RepeatFileInfo>> fileMap = new HashMap<>()
    List<String> repeatFiles = new ArrayList<>()

    @Override
    void onAnalysisFile(Library lib, ZipFile file, ZipEntry entry) {
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

    @Override
    String outputReport() {
        if (repeatFiles.isEmpty()) {
            return ""
        }
        repeatFiles.sort(new Comparator<String>() {
            @Override
            int compare(String s, String t1) {
                return s <=> t1
            }
        })

        StringBuilder builder = new StringBuilder()
        builder.append("## Repeat Files").append("\r\n\r\n")
        repeatFiles.each {
            Set<RepeatFileInfo> fileInfos = fileMap.get(it)
            if (fileInfos != null) {
                builder.append("* ").append(it).append("\r\n")
                fileInfos.each {
                    info ->
                        builder.append("\t* ").append(info.id).append(" (")
                                .append(FileUtils.convertFileSize(info.fileSize)).append(")\r\n")
                }
            } else {
                builder.append("* ${it} Not found its librarys. Is it a repeat file? \r\n")
            }
        }
        return builder.toString()
    }
}
