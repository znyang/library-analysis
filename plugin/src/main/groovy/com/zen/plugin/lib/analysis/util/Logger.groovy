package com.zen.plugin.lib.analysis.util;

/**
 * @author zen
 * @version 2016/9/10
 */

class Logger {

    boolean isEnable

    Logger(boolean isEnable) {
        this.isEnable = isEnable
    }

    void d(def message) {
        if (isEnable) {
            println message
        }
    }
}
