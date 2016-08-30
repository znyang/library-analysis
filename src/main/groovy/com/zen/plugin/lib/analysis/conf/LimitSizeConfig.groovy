package com.zen.plugin.lib.analysis.conf;

/**
 * @author zen
 * @version 2016/6/21
 */
class LimitSizeConfig {

    static final long DEFAULT_LIB_SIZE_LIMIT = 1024 * 1024;
    static final long DEFAULT_FILE_SIZE_LIMIT = 100 * 1024;

    private long fileSize = DEFAULT_FILE_SIZE_LIMIT;
    private long libSize = DEFAULT_LIB_SIZE_LIMIT;

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getLibSize() {
        return libSize;
    }

    public void setLibSize(long libSize) {
        this.libSize = libSize;
    }
}
