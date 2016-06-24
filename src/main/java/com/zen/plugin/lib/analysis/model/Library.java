package com.zen.plugin.lib.analysis.model;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.dependency.JarDependency;
import com.android.builder.dependency.LibraryDependency;
import com.zen.plugin.lib.analysis.FileUtils;
import com.zen.plugin.lib.analysis.comparator.FullSizeComparator;
import com.zen.plugin.lib.analysis.comparator.MixedComparator;
import com.zen.plugin.lib.analysis.comparator.SizeComparator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.ZipEntry;

/**
 * @author zen
 * @version 2016/6/5
 */
public class Library {
    /**
     * 下级Jar依赖库
     */
    List<JarDependency> mJarDependencies = new ArrayList<>();
    /**
     * 下级依赖库集合
     */
    List<Library> mLibraries = new ArrayList<>();
    /**
     * 下级依赖库实例集合（不含被忽略的部分）
     */
    Set<LibraryDependency> mDependencySet = new HashSet<>();
    /**
     * 被忽略的依赖库
     */
    Set<LibraryDependency> mIgnoreDependencySet = new HashSet<>();
    /**
     * 依赖库统计大小
     */
    long mSize = -1;
    boolean mIsLast = true;
    /**
     * 当前库是否被忽略
     */
    boolean mIsIgnore = false;
    /**
     * 当前库对应的依赖实例
     */
    LibraryDependency mLibraryDependency;

    /**
     * aar中大文件集合
     */
    final List<ZipEntry> mLargeEntries = new ArrayList<>();

    @Nullable
    public LibraryDependency getLibraryDependency() {
        return mLibraryDependency;
    }

    public boolean isEmpty() {
        return mJarDependencies.isEmpty() && mLibraries.isEmpty() && mLibraryDependency == null;
    }

    /**
     * 计算分析依赖库结构、大小
     *
     * @param replay    是否重新计算
     * @param ignore    忽略表
     * @param limitSize 文件限定大小
     */
    public void computeDependencies(boolean replay, List<String> ignore, long limitSize) {
        computeDependencies(replay, ignore, limitSize, false);
    }

    /**
     * 计算分析依赖库结构、大小
     *
     * @param replay       是否重新计算
     * @param ignore       忽略表
     * @param limitSize    文件限定大小
     * @param parentIgnore 父节点是否被忽略
     */
    public void computeDependencies(boolean replay, List<String> ignore, long limitSize, boolean parentIgnore) {
        if (!mDependencySet.isEmpty() && !replay) {
            return;
        }
        if (mLibraryDependency != null) {
            String name = mLibraryDependency.getName();
            if (mIsIgnore = parentIgnore | checkIgnore(ignore, name)) {
                mIgnoreDependencySet.add(mLibraryDependency);
            } else {
                mDependencySet.add(mLibraryDependency);
            }
        }

        // 如果当前依赖库被忽略了，那么它所依赖的库也被忽略
        Set<LibraryDependency> dependencies = mIsIgnore ? mIgnoreDependencySet : mDependencySet;
        for (Library lib : mLibraries) {
            lib.computeDependencies(false, ignore, limitSize, mIsIgnore);
            dependencies.addAll(lib.mDependencySet);
        }

        computeSize();
        collectLargeFiles(limitSize);
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

    public SortedSet<FileWrapper> findAllLargeFileWrapper() {
        SortedSet<FileWrapper> set = new TreeSet<>(new MixedComparator());
        for (ZipEntry entry : mLargeEntries) {
            set.add(new FileWrapper(mLibraryDependency, entry));
        }
        for (Library lib : mLibraries) {
            set.addAll(lib.findAllLargeFileWrapper());
        }
        return set;
    }

    /**
     * aar大小的排序集合
     *
     * @return
     */
    public SortedSet<FileWrapper> findAllDependencyAarWrapper() {
        SortedSet<FileWrapper> fileWrappers = new TreeSet<>(new SizeComparator());
        if (mLibraryDependency != null) {
            fileWrappers.add(new FileWrapper(mLibraryDependency));
        }
        for (Library lib : mLibraries) {
            fileWrappers.addAll(lib.findAllDependencyAarWrapper());
        }
        return fileWrappers;
    }

    /**
     * 依赖库统计大小的排序集合
     *
     * @return
     */
    public SortedSet<FileWrapper> findAllDependencySizeWrapper() {
        SortedSet<FileWrapper> fileWrappers = new TreeSet<>(new SizeComparator());
        if (mLibraryDependency != null) {
            fileWrappers.add(new FileWrapper(mLibraryDependency, getSize()));
        }
        for (Library lib : mLibraries) {
            fileWrappers.addAll(lib.findAllDependencySizeWrapper());
        }
        return fileWrappers;
    }

    public SortedSet<DependencyWrapper> findAllDependencyWrapper() {
        SortedSet<DependencyWrapper> fileWrappers = new TreeSet<>(new FullSizeComparator());
        if (mLibraryDependency != null) {
            fileWrappers.add(new DependencyWrapper(mLibraryDependency.getName(), getSize(),
                    getLibraryDependencyBundleSize(mLibraryDependency)));
        }
        for (Library lib : mLibraries) {
            fileWrappers.addAll(lib.findAllDependencyWrapper());
        }
        return fileWrappers;
    }

    private static long getLibraryDependencyBundleSize(LibraryDependency dependency) {
        if (dependency == null) {
            return 0;
        }
        File bundle = dependency.getBundle();
        if (bundle == null) {
            return 0;
        }
        return bundle.length();
    }

    /**
     * 收集当前aar中大于等于指定大小的文件
     *
     * @param limitSize
     */
    private void collectLargeFiles(long limitSize) {
        if (mIsIgnore || mLibraryDependency == null) {
            return;
        }

        File bundle = mLibraryDependency.getBundle();
        if (bundle == null || !bundle.exists()) {
            return;
        }

        String type = FileUtils.getFileType(bundle.getName());
        if (!"aar".equals(type)) {
            return;
        }

        synchronized (mLargeEntries) {
            mLargeEntries.clear();

            try {
                AarFile aar = new AarFile(bundle);
                mLargeEntries.addAll(aar.findLargeFiles(limitSize));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @NonNull
    public List<ZipEntry> getLargeEntries() {
        return mLargeEntries;
    }

    /**
     * 统计未被忽略的依赖库大小
     */
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
