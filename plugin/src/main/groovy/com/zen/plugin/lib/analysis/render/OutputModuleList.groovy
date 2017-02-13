package com.zen.plugin.lib.analysis.render

import com.zen.plugin.lib.analysis.util.FileUtils

/**
 * OutputModuleList
 *
 * @author znyang 2017/2/13 0013
 */

public class OutputModuleList {

    List<DependencyOutput> modules = new ArrayList<>()

    public void sortModules() {
        modules.sort {
            first, two ->
                two.sizeValue - first.sizeValue
        }
    }

    public void addModule(DependencyOutput output) {
        if (!modules.contains(output)) {
            modules.add(output)
        }
    }

    public static class DependencyOutput {

        String name
        String size
        String type
        String pkgName
        String extInfo
        long sizeValue

        DependencyOutput(String name, long size, String pkgName, String type, String extInfo) {
            this.name = name
            this.sizeValue = size
            this.size = FileUtils.convertFileSize(size)
            this.pkgName = pkgName
            this.type = type
            this.extInfo = extInfo
        }

        boolean equals(o) {
            if (this.is(o)) return true
            if (getClass() != o.class) return false

            DependencyOutput that = (DependencyOutput) o

            if (name != that.name) return false

            return true
        }

        int hashCode() {
            return (name != null ? name.hashCode() : 0)
        }
    }

}
