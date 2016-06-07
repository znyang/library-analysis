# Library Analysis Gradle Plugin

[![Build Status](https://travis-ci.org/znyang/library-analysis.svg?branch=master)](https://travis-ci.org/znyang/library-analysis)
[![](https://jitpack.io/v/znyang/library-analysis.svg)](https://jitpack.io/#znyang/library-analysis)
[![codecov.io](https://codecov.io/github/znyang/library-analysis/coverage.svg?branch=feature-ut)](https://codecov.io/gh/znyang/library-analysis/branch/feature-ut)

## 概述

分析并统计依赖库文件的占用大小。

1. 支持大文件提醒
2. 支持忽略部分依赖库大小（不统计）

## 配置

```gradle
buildscript {
    dependencies {
        classpath 'com.github.znyang:library-analysis:0.0.1-beta2'
    }
}

apply plugin: 'com.zen.lib.analysis'

libReport {
    fileSizeLimit = 100 * 1024 // aar包中的文件比该数值大小的将会被列出
    sizeLimit = 1024 * 1024 // aar,jar文件超过该数值大小的会被红色标记
    ignore = [
            "com.android.support:support-v4"
    ]
}
```

## 使用

```
gradle libraryReportDebug
```

output:

```
debug
ignore - com.android.support:support-v4
Dependencies size: 956 KB
+--- [927 KB] com.android.support:appcompat-v7:23.3.0@aar(877 KB)
|    +--- Large Files:
|    |    \--- res/values/values.xml - 113 KB
|    +--- [38.405 KB] com.android.support:support-vector-drawable:23.3.0@aar(38.405 KB)
|    |    \--- [ignore] com.android.support:support-v4:23.3.0@aar(1.169 MB)
|    |         \--- LOCAL: internal_impl-23.3.0.jar(315 KB)
|    +--- [49.753 KB] com.android.support:animated-vector-drawable:23.3.0@aar(11.348 KB)
|    |    \--- [38.405 KB] com.android.support:support-vector-drawable:23.3.0@aar(38.405 KB)
|    |         \--- [ignore] com.android.support:support-v4:23.3.0@aar(1.169 MB)
|    |              \--- LOCAL: internal_impl-23.3.0.jar(315 KB)
|    \--- [ignore] com.android.support:support-v4:23.3.0@aar(1.169 MB)
|         \--- LOCAL: internal_impl-23.3.0.jar(315 KB)
+--- [942 KB] sample-resource-conflict:library-b:unspecified@aar(14.726 KB)
|    \--- [927 KB] com.android.support:appcompat-v7:23.3.0@aar(877 KB)
|         +--- Large Files:
|         |    \--- res/values/values.xml - 113 KB
|         +--- [38.405 KB] com.android.support:support-vector-drawable:23.3.0@aar(38.405 KB)
|         |    \--- [ignore] com.android.support:support-v4:23.3.0@aar(1.169 MB)
|         |         \--- LOCAL: internal_impl-23.3.0.jar(315 KB)
|         +--- [49.753 KB] com.android.support:animated-vector-drawable:23.3.0@aar(11.348 KB)
|         |    \--- [38.405 KB] com.android.support:support-vector-drawable:23.3.0@aar(38.405 KB)
|         |         \--- [ignore] com.android.support:support-v4:23.3.0@aar(1.169 MB)
|         |              \--- LOCAL: internal_impl-23.3.0.jar(315 KB)
|         \--- [ignore] com.android.support:support-v4:23.3.0@aar(1.169 MB)
|              \--- LOCAL: internal_impl-23.3.0.jar(315 KB)
\--- [941 KB] sample-resource-conflict:library-a:unspecified@aar(14.218 KB)
     \--- [927 KB] com.android.support:appcompat-v7:23.3.0@aar(877 KB)
          +--- Large Files:
          |    \--- res/values/values.xml - 113 KB
          +--- [38.405 KB] com.android.support:support-vector-drawable:23.3.0@aar(38.405 KB)
          |    \--- [ignore] com.android.support:support-v4:23.3.0@aar(1.169 MB)
          |         \--- LOCAL: internal_impl-23.3.0.jar(315 KB)
          +--- [49.753 KB] com.android.support:animated-vector-drawable:23.3.0@aar(11.348 KB)
          |    \--- [38.405 KB] com.android.support:support-vector-drawable:23.3.0@aar(38.405 KB)
          |         \--- [ignore] com.android.support:support-v4:23.3.0@aar(1.169 MB)
          |              \--- LOCAL: internal_impl-23.3.0.jar(315 KB)
          \--- [ignore] com.android.support:support-v4:23.3.0@aar(1.169 MB)
               \--- LOCAL: internal_impl-23.3.0.jar(315 KB)

```