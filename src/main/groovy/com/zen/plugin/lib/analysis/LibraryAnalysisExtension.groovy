package com.zen.plugin.lib.analysis
/**
 * @author zen
 * @version 2016/5/29
 */
class LibraryAnalysisExtension {

    static final String ANALYSIS_OUTPUT_PATH = "report/zen/analysis/library";
    static final long DEFAULT_SIZE_LIMIT = 1024 * 1024;

    private boolean verbose
    private String outputPath = ANALYSIS_OUTPUT_PATH
    private List<String> ignore
    private long sizeLimit = DEFAULT_SIZE_LIMIT;

    long getSizeLimit() {
        return sizeLimit
    }

    void setSizeLimit(long sizeLimit) {
        this.sizeLimit = sizeLimit
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
