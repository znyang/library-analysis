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

class DependencyDictionary {

    static final def                  SEPARATOR    = File.separator
    static final def                  BUILD_DIR    = "build${SEPARATOR}outputs${SEPARATOR}aar${SEPARATOR}"
    final FileCollection              fileCollection
          Set<File>                   files
    final Map<String, File>           cacheFiles   = new HashMap<>()
    final Map<String, DependencyInfo> cacheInfoMap = new HashMap<>()
          long                        totalSize    = 0L
          long                        maxSize      = 0L

    DependencyDictionary(FileCollection fileCollection) {
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

    File findDependency(String dependencyId) {
        if (cacheFiles.containsKey(dependencyId)) {
            return cacheFiles.get(dependencyId)
        }

        def dependency = dependencyId.split("\\:")
        def size = dependency.size()
        def result = null
        Logger.D?.log "find start: ${dependencyId}"

        if (size == 3) {
            def key = dependency.join(SEPARATOR)
            def group = dependency[0].replace('.', SEPARATOR)
            def keyFull = "${group}${SEPARATOR}${dependency[1]}${SEPARATOR}${dependency[2]}"

            result = getFiles().find {
                Logger.D?.log "find ${it.path} ==? ${key} ==? ${keyFull}"
                it.path.contains(key) || it.path.contains(keyFull)
            }
        } else if (size == 2) {
            def key = "${BUILD_DIR}${dependency[1]}"

            result = getFiles().find {
                Logger.D?.log "find ${it.path} ==? ${key}"
                it.path.contains(key)
            }
        }
        if (result != null) {
            Logger.D?.log "find result: " + result.path
            cacheFiles.put(dependencyId, result)
            files.remove(result)
        } else {
            Logger.D?.log "not found ${dependencyId}"
        }
        result
    }

    DependencyInfo findDependencyInfo(String dependencyId) {
        def info = cacheInfoMap.get(dependencyId)
        if (info == null) {
            File file = findDependency(dependencyId)
            info = putCache(dependencyId, file)
        }
        info
    }

    DependencyInfo putCache(String id, File file) {
        if (file == null) {
            return null
        }
        DependencyInfo info = new DependencyInfo(id, file.size(), FileUtils.getFileType(file.name))
        cacheInfoMap.put(id, info)
        info
    }

}
