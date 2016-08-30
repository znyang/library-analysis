package com.zen.plugin.lib.analysis.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author zen
 * @version 2016/6/7
 */
public class AarFile {

    final ZipFile mAarFile;
    final List<ZipEntry> mEntries = new ArrayList<>();

    public AarFile(File aar) throws IOException {
        mAarFile = new ZipFile(aar);
    }

    public List<ZipEntry> getmEntries() {
        if (mEntries.isEmpty()) {
            mEntries.clear();
            Enumeration<? extends ZipEntry> enumeration = mAarFile.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry entry = enumeration.nextElement();
                mEntries.add(entry);
            }
        }
        return mEntries;
    }

    public List<ZipEntry> findLargeFiles(long limitSize) {
        List<ZipEntry> entries = new ArrayList<>();
        Enumeration<? extends ZipEntry> enumeration = mAarFile.entries();
        while (enumeration.hasMoreElements()) {
            ZipEntry entry = enumeration.nextElement();
            String name = entry.getName();
            // 忽略jar文件/R.txt/value.xml
            if (name.endsWith(".jar") || name.equals("R.txt") || name.equals("res/values/values.xml")) {
                continue;
            }
            if (entry.getSize() >= limitSize) {
                entries.add(entry);
            }
        }
        return entries;
    }
}
