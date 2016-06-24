package com.zen.plugin.lib.analysis.comparator;

import com.zen.plugin.lib.analysis.model.DependencyWrapper;

import java.util.Comparator;

/**
 * @author zen
 * @version 2016/6/24
 */
public class FullSizeComparator implements Comparator<DependencyWrapper> {
    @Override
    public int compare(DependencyWrapper o1, DependencyWrapper o2) {
        if (o2 != null) {
            long os1 = o1.getAllSize();
            long os2 = o2.getAllSize();
            if (os1 < os2) {
                return 1;
            } else if (os1 == os2) {
                return 0;
            }
        }
        return -1;
    }
}