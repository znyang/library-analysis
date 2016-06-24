package com.zen.plugin.lib.analysis.model;


public class DependencyWrapper {

    private String mDependency;
    private long mAllSize;
    private long mAarSize;

    public DependencyWrapper(String dependency, long allSize, long aarSize) {
        mDependency = dependency;
        mAllSize = allSize;
        mAarSize = aarSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DependencyWrapper that = (DependencyWrapper) o;

        if (mAllSize != that.mAllSize) return false;
        if (mAarSize != that.mAarSize) return false;
        return mDependency != null ? mDependency.equals(that.mDependency) : that.mDependency == null;

    }

    @Override
    public int hashCode() {
        int result = mDependency != null ? mDependency.hashCode() : 0;
        result = 31 * result + (int) (mAllSize ^ (mAllSize >>> 32));
        result = 31 * result + (int) (mAarSize ^ (mAarSize >>> 32));
        return result;
    }

    public String getDependency() {
        return mDependency;
    }

    public void setDependency(String dependency) {
        mDependency = dependency;
    }

    public long getAllSize() {
        return mAllSize;
    }

    public void setAllSize(long allSize) {
        mAllSize = allSize;
    }

    public long getAarSize() {
        return mAarSize;
    }

    public void setAarSize(long aarSize) {
        mAarSize = aarSize;
    }
}
