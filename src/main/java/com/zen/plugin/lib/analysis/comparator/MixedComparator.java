package com.zen.plugin.lib.analysis.comparator;

import com.zen.plugin.lib.analysis.model.FileWrapper;

import java.util.Comparator;

/**
 * @author zen
 * @version 2016/6/24
 */
public class MixedComparator implements Comparator<FileWrapper> {
    @Override
    public int compare(FileWrapper o1, FileWrapper o2) {
        if (o2 != null) {
            String dependency1 = o1.getDependency();
            String dependency2 = o2.getDependency();

            if (!dependency1.equals(dependency2)) {
                return dependency1.compareTo(dependency2);
            }

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