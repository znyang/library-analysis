package com.zen.plugin.lib.analysis.conf

import org.gradle.util.ConfigureUtil

/**
 * @author zen
 * @version 2016/5/29
 */
class LibraryAnalysisExtension {

    static final String ANALYSIS_OUTPUT_PATH = "report/zen/analysis/library";
    private static final String STR_MARK = " <<<";

    private boolean verbose
    private String outputPath = ANALYSIS_OUTPUT_PATH
    private List<String> ignore
    public final LimitSizeConfig limit
    private String mark = STR_MARK

    LibraryAnalysisExtension() {
        limit = new LimitSizeConfig()
    }

    void limit(Closure closure) {
        ConfigureUtil.configure(closure, limit)
    }

    String getMark() {
        return mark
    }

    void setMark(String mark) {
        this.mark = mark
    }

    boolean getVerbose() {
        return verbose
    }

    void setVerbose(boolean verbose) {
        this.verbose = verbose
    }

    String getOutputPath() {
        return outputPath
    }

    void setOutputPath(String outputPath) {
        this.outputPath = outputPath
    }

    List<String> getIgnore() {
        return ignore
    }

    void setIgnore(List<String> ignore) {
        this.ignore = ignore
    }
}
