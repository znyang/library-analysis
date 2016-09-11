package com.zen.plugin.lib.analysis.util;

/**
 * @author zen
 * @version 2016/9/10
 */

class Logger {

    static Logger D = new Logger()

    private Logger() {
    }

    void log(def message) {
        println message
    }
}
