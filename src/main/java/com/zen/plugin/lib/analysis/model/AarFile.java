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

    ZipFile file;
    List<ZipEntry> entries = new ArrayList<>();

    public AarFile(File aar) throws IOException {
        file = new ZipFile(aar);
    }

    public List<ZipEntry> getEntries() {
        if (entries.isEmpty()) {
            entries.clear();
            Enumeration<? extends ZipEntry> enumeration = file.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry entry = enumeration.nextElement();
                entries.add(entry);
            }
        }
        return entries;
    }
}
