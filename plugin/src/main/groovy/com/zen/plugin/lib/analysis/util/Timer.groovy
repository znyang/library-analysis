package com.zen.plugin.lib.analysis.util;

/**
 * @author zen
 * @version 2016/9/10
 */

class Timer {

    long start

    Timer() {
        start = System.currentTimeMillis()
    }

    void mark(Logger logger, String message) {
        long now = System.currentTimeMillis()
        logger?.d "${message ?: ""} cost ${now - start}ms"
        start = now
    }

}
