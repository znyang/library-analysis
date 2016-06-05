package com.zen.plugin.lib.analysis.model;

import com.android.annotations.Nullable;
import com.android.builder.dependency.JarDependency;
import com.android.builder.dependency.LibraryDependency;

import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author zen
 * @version 2016/6/5
 */
public class Library {
    List<JarDependency>    mJarDependencies     = new ArrayList<>();
    List<Library>          mLibraries           = new ArrayList<>();
    Set<LibraryDependency> mDependencySet       = new HashSet<>();
    Set<LibraryDependency> mIgnoreDependencySet = new HashSet<>();
    long                   mSize                = -1;
    boolean                mIsLast              = true;
    boolean                mIsIgnore            = false;
    LibraryDependency mLibraryDependency;

    @Nullable
    public LibraryDependency getLibraryDependency() {
        return mLibraryDependency;
    }

    public boolean isEmpty() {
        return mJarDependencies.isEmpty() && mLibraries.isEmpty() && mLibraryDependency == null;
    }

    public void computeDependencies(boolean replay, List<String> ignore) {
        if (!mDependencySet.isEmpty() && !replay) {
            return;
        }
        if (mLibraryDependency != null) {
            String name = mLibraryDependency.getName();
            if (mIsIgnore = checkIgnore(ignore, name)) {
                mIgnoreDependencySet.add(mLibraryDependency);
            } else {
                mDependencySet.add(mLibraryDependency);
            }
        }

        for (Library lib : mLibraries) {
            lib.computeDependencies(false, ignore);
            mDependencySet.addAll(lib.mDependencySet);
        }

        computeSize();
    }

    private boolean checkIgnore(List<String> ignore, String name) {
        boolean isIgnore = false;
        if (ignore != null && !ignore.isEmpty()) {
            for (String ig : ignore) {
                if (isIgnore = (name.contains(ig))) {
                    break;
                }
            }
        }
        return isIgnore;
    }

    private void computeSize() {
        long length = 0;
        for (LibraryDependency dependency : mDependencySet) {
            length += dependency.getBundle().length();
        }
        mSize = length;
    }

    public List<JarDependency> getJarDependencies() {
        return mJarDependencies;
    }

    public List<Library> getLibraries() {
        return mLibraries;
    }

    public long getSize() {
        return mSize;
    }

    public boolean isLast() {
        return mIsLast;
    }

    public void setLibraryDependency(LibraryDependency libraryDependency) {
        this.mLibraryDependency = libraryDependency;
    }

    public void setLast(boolean last) {
        mIsLast = last;
    }

    public boolean isIgnore() {
        return mIsIgnore;
    }
}
