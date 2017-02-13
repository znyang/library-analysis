package com.zen.plugin.lib.analysis.model
/**
 * Node
 *
 * @author yangz
 * @version 2016/7/8
 */
class DependencyInfo {
    String id
    long size
    String type
    File file

    DependencyInfo(String id, long size, String type, File file) {
        this.id = id
        this.size = size
        this.type = type
        this.file = file
    }

}
