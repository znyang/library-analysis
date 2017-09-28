package com.zen.plugin.lib.analysis.model

import com.zen.plugin.lib.analysis.util.FileUtils
import com.zen.plugin.lib.analysis.util.Logger
import org.gradle.api.file.FileCollection

/**
 *
 * 用于对指定id的依赖库查询其文件信息(主要是大小)
 *
 * @author zen
 * @version 2016/9/10
 */

class FileDictionary {

    static final SEPARATOR = File.separator
    static final BUILD_DIR = "build${SEPARATOR}outputs${SEPARATOR}aar${SEPARATOR}"
    final FileCollection fileCollection
    Set<File> files
    final Map<String, File> cacheFiles = new HashMap<>()
    final Map<String, FileInfo> cacheInfoMap = new HashMap<>()
    long totalSize = 0L
    long maxSize = 0L
    long totalFindCount = 0l

    FileDictionary(FileCollection fileCollection) {
        this.fileCollection = fileCollection
        computeTotalSize()
    }

    Set<File> getFiles() {
        if (files == null) {
            files = fileCollection.getFiles()
        }
        files
    }

    void computeTotalSize() {
        totalSize = 0
        getFiles().each {
            totalSize += it.size()
            if (it.size() > maxSize) {
                maxSize = it.size()
            }
        }
    }

    /**
     * 根据依赖库ID查找本地文件
     * @param dependencyId
     * @return
     */
    File findDependencyFile(String dependencyId) {
        if (cacheFiles.containsKey(dependencyId)) {
            return cacheFiles.get(dependencyId)
        }

        def dependency = dependencyId.split("\\:")
        def size = dependency.size()
        def result = null

//        def sep = "\r\n"
//        StringBuilder builder = new StringBuilder()
//        builder.append("find start: ${dependencyId}").append(sep)
        int count = 0

        if (size == 3) {
            def key = dependency.join(SEPARATOR)
            def group = dependency[0].replace('.', SEPARATOR)
            def keyFull = "${group}${SEPARATOR}${dependency[1]}${SEPARATOR}${dependency[2]}"

            result = getFiles().find {
                count++
//                builder.append("find ${it.path} ==? ${key} ==? ${keyFull}").append(sep)
                it.path.contains(keyFull) || it.path.contains(key)
            }
        } else if (size == 2) {
            def key = "${BUILD_DIR}${dependency[1]}"

            result = getFiles().find {
                count++
//                builder.append("find ${it.path} ==? ${key}").append(sep)
                it.path.contains(key)
            }
        }

        totalFindCount += count

        if (result != null) {
//            Logger.W?.log "find result: " + result.path + " tcount:" + totalFindCount
            cacheFiles.put(dependencyId, result)
            files.remove(result)
        } else {
            Logger.W?.log "not found ${dependencyId} jar/aar file."
//            Logger.W?.log builder.toString()
        }
        result
    }

    /**
     * 根据依赖库ID获取相关信息
     * @param dependencyId
     * @return
     */
    FileInfo findDependencyInfo(String dependencyId) {
        def info = cacheInfoMap.get(dependencyId)
        if (info == null) {
            File file = findDependencyFile(dependencyId)
            info = putCache(dependencyId, file)
        }
        info
    }

    FileInfo putCache(String id, File file) {
        if (file == null) {
            return null
        }
        FileInfo info = new FileInfo(id, file.size(), FileUtils.getFileType(file.name), file)
        cacheInfoMap.put(id, info)
        info
    }

}
