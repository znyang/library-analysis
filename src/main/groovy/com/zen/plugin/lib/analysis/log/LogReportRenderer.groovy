package com.zen.plugin.lib.analysis.log;

import com.zen.plugin.lib.analysis.log.ILog;

import org.gradle.api.tasks.diagnostics.internal.TextReportRenderer;

import java.io.File;

/**
 * @author zen
 * @version 2016/6/28
 */
public class LogReportRenderer extends TextReportRenderer {

    private final String mOutputPath;
    private final String mFileName;

    LogReportRenderer(String fileName, String outputPath) {
        mFileName = fileName
        mOutputPath = outputPath

        setOutputFile(prepareOutputFile())
    }

    public void renderLog(ILog logger) {
        getTextOutput().text(logger.getLog());
    }

    private File prepareOutputFile() {
        def path = mOutputPath + "/log"
        new File(path).mkdirs();

        File analysisFile = new File(path, mFileName);
        if (analysisFile.exists()) {
            analysisFile.delete()
        }
        analysisFile.createNewFile()
        analysisFile
    }

}
