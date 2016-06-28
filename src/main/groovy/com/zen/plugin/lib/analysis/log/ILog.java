package com.zen.plugin.lib.analysis.log;

/**
 * @author zen
 * @version 2016/6/28
 */
public interface ILog {

    void d(String msg, Object... args);

    String getLog();

}
