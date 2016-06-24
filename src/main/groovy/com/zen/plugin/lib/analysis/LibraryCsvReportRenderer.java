package com.zen.plugin.lib.analysis;

import com.zen.plugin.lib.analysis.model.DependencyWrapper;
import com.zen.plugin.lib.analysis.model.FileWrapper;

import org.gradle.api.tasks.diagnostics.internal.TextReportRenderer;

import java.io.IOException;
import java.util.SortedSet;

/**
 * @author zen
 * @version 2016/6/4
 */
public class LibraryCsvReportRenderer extends TextReportRenderer {

    private static final String FORMAT_FILE_INFO = "%s,%d,%d";

    public void render(final SortedSet<DependencyWrapper> files) throws IOException {
        getTextOutput().println("Dependency,All,Aar");
        for (DependencyWrapper file : files) {
            String dependency = file.getDependency();
            getTextOutput().formatln(FORMAT_FILE_INFO,
                    dependency, file.getAllSize(),
                    file.getAarSize());
        }
    }
}
