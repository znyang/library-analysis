package com.zen.plugin.lib.analysis;

import com.zen.plugin.lib.analysis.model.FileWrapper;
import com.zen.plugin.lib.analysis.model.Library;

import org.gradle.api.tasks.diagnostics.internal.TextReportRenderer;

import java.io.IOException;
import java.util.SortedSet;

/**
 * @author zen
 * @version 2016/6/4
 */
public class LibraryLimitReportRenderer extends TextReportRenderer {

    private static final String FORMAT_FILE_INFO = "| %s | %s | %s |";

    public void render(final SortedSet<FileWrapper> files) throws IOException {
        getTextOutput().println("| Dependency | File | Size |");
        getTextOutput().println("| :--- | :--- | :--- |");
        for (FileWrapper file : files) {
            String dependency = file.getDependency();
            getTextOutput().formatln(FORMAT_FILE_INFO,
                    dependency, file.getFileName(),
                    FileUtils.convertFileSize(file.getSize()));
        }
    }
}
