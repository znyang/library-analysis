package com.zen.plugin.lib.analysis.comparator;

import com.zen.plugin.lib.analysis.model.FileWrapper;

import java.util.Comparator;

/**
 * @author zen
 * @version 2016/6/23
 */
public class SizeComparator implements Comparator<FileWrapper> {
    @Override
    public int compare(FileWrapper o1, FileWrapper o2) {
        if (o2 != null) {
            long os1 = o1.getSize();
            long os2 = o2.getSize();
            if (os1 < os2) {
                return 1;
            } else if (os1 == os2) {
                return 0;
            }
        }
        return -1;
    }
}
