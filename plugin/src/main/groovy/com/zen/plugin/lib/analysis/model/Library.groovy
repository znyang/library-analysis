package com.zen.plugin.lib.analysis.model

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

    static Library create(RenderableDependency dependency, FileDictionary dictionary) {
        create(dependency, dictionary, new HashMap<>())
    }

    static Library create(RenderableDependency dependency, FileDictionary dictionary, Map<Object, Library> cache) {
        String id = dependency.id
        Library lib = cache.get(id)
        if (lib) {
            lib.addUseCount()
            return lib
        } else {
            def result = new Library(dependency.name, id)
            cache.put(id, result)

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
                size += it.file?.size
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
