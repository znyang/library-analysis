package com.zen.plugin.lib.analysis;

/**
 * @author zen
 * @version 2016/6/5
 */
public final class FileUtils {

    public static String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.3f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.3f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.3f KB", f);
        } else
            return String.format("%d B", size);
    }

    public static String getFileType(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index >= 0) {
            return fileName.substring(index + 1);
        }
        return fileName;
    }

}
