package com.zen.plugin.lib.analysis.model;

import com.android.builder.dependency.LibraryDependency;

import java.util.zip.ZipEntry;

/**
 * @author zen
 * @version 2016/6/22
 */
public class FileWrapper implements Comparable<FileWrapper> {

    private String mDependency;
    private String mFileName;
    private long mSize;

    public FileWrapper(LibraryDependency dependency, ZipEntry entry) {
        mDependency = dependency.getName();
        mFileName = entry.getName();
        mSize = entry.getSize();
    }

    public String getDependency() {
        return mDependency;
    }

    public void setDependency(String dependency) {
        this.mDependency = dependency;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        this.mFileName = fileName;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        this.mSize = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileWrapper fileWrapper = (FileWrapper) o;

        if (mSize != fileWrapper.mSize) return false;
        if (mDependency != null ? !mDependency.equals(fileWrapper.mDependency) : fileWrapper.mDependency != null) {
            return false;
        }
        return mFileName != null ? mFileName.equals(fileWrapper.mFileName) : fileWrapper.mFileName == null;
    }

    @Override
    public int hashCode() {
        int result = mDependency != null ? mDependency.hashCode() : 0;
        result = 31 * result + (mFileName != null ? mFileName.hashCode() : 0);
        result = 31 * result + (int) (mSize ^ (mSize >>> 32));
        return result;
    }

    @Override
    public int compareTo(FileWrapper o) {
        if (o != null) {
            long os = o.getSize();
            if (getSize() < os) {
                return 1;
            } else if (getSize() == os) {
                return 0;
            }
        }
        return -1;
    }

}
