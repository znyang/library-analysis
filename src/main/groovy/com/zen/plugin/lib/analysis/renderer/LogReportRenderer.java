package com.zen.plugin.lib.analysis.renderer;

import com.zen.plugin.lib.analysis.log.ILog;

import org.gradle.api.tasks.diagnostics.internal.TextReportRenderer;

/**
 * @author zen
 * @version 2016/6/28
 */
public class LogReportRenderer extends TextReportRenderer {

    public void renderLog(ILog logger) {
        getTextOutput().text(logger.getLog());
    }

}
