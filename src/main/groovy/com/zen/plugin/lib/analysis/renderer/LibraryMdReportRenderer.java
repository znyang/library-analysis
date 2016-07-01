package com.zen.plugin.lib.analysis.renderer;

import com.zen.plugin.lib.analysis.FileUtils;
import com.zen.plugin.lib.analysis.model.FileWrapper;

import org.gradle.api.tasks.diagnostics.internal.TextReportRenderer;

import java.io.IOException;
import java.util.SortedSet;

/**
 * @author zen
 * @version 2016/6/4
 */
public class LibraryMdReportRenderer extends TextReportRenderer {

    private static final String COL_DEPENDENCY = "| Dependency ";
    private static final String COL_FILE = "| File ";
    private static final String COL_SIZE = "| Size ";

    private static final String MD_COLUMN_CODE = "| :--- ";
    private static final String FORMAT_COLUMN_CONTENT = "| %s ";

    public LibraryMdReportRenderer() {
    }

    public void renderOnlyFileName(final SortedSet<FileWrapper> files) {
        String[] columns = new String[]{COL_FILE, COL_SIZE};
        renderTableColumnHeader(columns);

        String columnFormat = genColumnsFormat(columns.length, FORMAT_COLUMN_CONTENT);
        for (FileWrapper file : files) {
            getTextOutput().formatln(columnFormat, file.getFileName(),
                    FileUtils.convertFileSize(file.getSize()));
        }
    }

    public void render(final SortedSet<FileWrapper> files) throws IOException {
        String[] columns = new String[]{COL_DEPENDENCY, COL_FILE, COL_SIZE};
        renderTableColumnHeader(columns);

        String columnFormat = genColumnsFormat(columns.length, FORMAT_COLUMN_CONTENT);
        for (FileWrapper file : files) {
            getTextOutput().formatln(columnFormat,
                    file.getDependency(), file.getFileName(),
                    FileUtils.convertFileSize(file.getSize()));
        }
    }

    private void renderTableColumnHeader(String[] columns) {
        for (String column : columns) {
            getTextOutput().append(column);
        }
        getTextOutput().append('|');
        getTextOutput().println();
        getTextOutput().println(genColumnsFormat(columns.length, MD_COLUMN_CODE));
    }

    private String genColumnsFormat(int size, String code) {
        StringBuilder builder = new StringBuilder();
        for (int i = size; --i >= 0; ) {
            builder.append(code);
        }
        builder.append('|');
        return builder.toString();
    }
}
