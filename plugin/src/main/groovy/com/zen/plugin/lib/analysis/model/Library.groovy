package com.zen.plugin.lib.analysis.model

import com.zen.plugin.lib.analysis.util.Logger
import com.zen.plugin.lib.analysis.util.Timer
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableDependency

/**
 * 依赖库模型
 *
 * @author: zen
 * date: 2017/6/26 0026.
 */
class Library {

    String name
    String id
    List<Library> children = new ArrayList<>()
    // 该库包含的所有子库
    Set<Library> contains = new HashSet<>()
    // 该库包含的所有子库的id集合
    Set<String> containIds = new HashSet<>()
    // 被使用次数
    int useCount = 1
    // 被使用次数(直接依赖)
    int useCountImmediate = 1
    // 引用的文件信息
    FileInfo file
    // 是否忽略文件大小
    boolean isIgnoreSize

    Library(String name, String id) {
        this.name = name
        this.id = id
    }

    synchronized void addUseCount() {
        useCount++
        children.each {
            it.addUseCount()
        }
    }

    static Library create(RenderableDependency root, FileDictionary dictionary) {
        def timer = new Timer()

        Map<Object, Library> libraries = new HashMap<>()
        def libRoot = create(root, dictionary, libraries)

        timer.mark(Logger.W, "create libRoot")

        // 合并useCount TODO: 可优化
        Map<String, Integer> useCounts = new HashMap<>()
        // 被直接依赖的次数
        Map<String, Integer> useCountImmediacies = new HashMap<>()

        parseLibraryUseCount(libRoot, useCountImmediacies)

        timer.mark(Logger.W, "parseLibraryUseCount")

        libraries.values().each {
            def id = it.id

            def count = useCounts.get(id)
            def immediate = useCountImmediacies.get(id)
            if (count && immediate) {
                count += it.useCount
                useCounts.put(id, count)
            } else {
                useCounts.put(id, it.useCount)
            }
        }

        libraries.values().each {
            if (useCounts.containsKey(it.id)) {
                it.useCount = useCounts.get(it.id)
            }
            if (useCountImmediacies.containsKey(it.id)) {
                it.useCountImmediate = useCountImmediacies.get(it.id)
            }
        }

        timer.mark(Logger.W, "compute libraries")

        libRoot
    }

    static void parseLibraryUseCount(Library library, Map<String, Integer> useCounts) {
        String id = library.id
        boolean hasAdded = useCounts.containsKey(id)
        if (!hasAdded) {
            useCounts.put(id, 1)
        } else {
            useCounts.put(id, useCounts.get(id) + 1)
        }

        if (!hasAdded && !library.children.isEmpty()) {
            library.children.each {
                parseLibraryUseCount(it, useCounts)
            }
        }
    }


    static Library create(RenderableDependency dependency, FileDictionary dictionary, Map<Object, Library> cache) {
        String id = dependency.id
        String name = dependency.name
//        Library lib = cache.get(id)
        Library nLib = cache.get(name)

        if (nLib) {
            nLib.addUseCount()
            return nLib
        } else {
            def result = new Library(name, id)
            cache.put(name, result)

            result.file = dictionary?.findDependencyInfo(id)
            dependency.children.each {
                result.children.add(create(it, dictionary, cache))
            }
            result.computeContains()
            return result
        }
    }

    /**
     * 解析该依赖库包含的所有子库
     */
    void computeContains() {
        if (!contains.isEmpty()) {
            return
        }
        children.each {
            it.computeContains()
            contains.add(it)
            contains.addAll(it.contains)

            containIds.add(it.id)
            containIds.addAll(it.containIds)
        }
    }

    long getTotalSize() {
        long size = file ? file.size : 0
        contains.each {
            size += it.file?.size
        }
        size
    }

    long getTotalSizeWithoutIgnore() {
        if (isIgnoreSize) {
            return 0
        }
        long size = file ? file.size : 0
        contains.each {
            if (!it.isIgnoreSize) {
                if (it.file) {
                    size += it.file.size
                }
            }
        }
        size
    }

    long getTotalSize(boolean isWithoutIgnore) {
        isWithoutIgnore ? getTotalSizeWithoutIgnore() : getTotalSize()
    }

    void applyIgnoreLibrary(String... ignores) {
        ignores.each {
            applyIgnoreLibrary(it)
        }
    }

    void applyIgnoreLibrary(String ignore) {
        if (isIgnoreSize || ignore == null) {
            return
        }
        isIgnoreSize = id.contains(ignore)
        if (isIgnoreSize) {
            children.each {
                it.isIgnoreSize = true
            }
        } else {
            children.each {
                it.applyIgnoreLibrary(ignore)
            }
        }
    }

    /**
     * 判断某个库是否是当前库或其依赖库
     * @param library
     * @return
     */
    boolean isContains(Library library) {
        return this == library || contains.contains(library)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Library library = (Library) o

        if (id != library.id) return false

        return true
    }

    int hashCode() {
        return (id != null ? id.hashCode() : 0)
    }
}
