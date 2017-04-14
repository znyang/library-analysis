# Library Analysis Gradle Plugin

[![Build Status](https://travis-ci.org/znyang/library-analysis.svg?branch=master)](https://travis-ci.org/znyang/library-analysis)
[![](https://jitpack.io/v/znyang/library-analysis.svg)](https://jitpack.io/#znyang/library-analysis)
[![codecov.io](https://codecov.io/github/znyang/library-analysis/coverage.svg?branch=master)](https://codecov.io/gh/znyang/library-analysis/branch/master)

## 概述

分析各依赖库文件的大小

1. 支持大文件提醒
2. 支持忽略部分依赖库大小（大小显示为灰色）
3. 支持依赖节点大小统计

## 配置

```gradle
buildscript {
    repositories {
        // ...
        maven { url "https://jitpack.io" }
    }
    dependencies {
        classpath 'com.github.znyang:library-analysis:0.1.7'
    }
}

apply plugin: 'com.zen.lib.analysis'

libReport {
    ignore = [
            "com.android.support:support-v4"
    ]
}
```

## 使用

```
gradle libReportCompile
```

### output

**/build/reports/zen/analysis/library/compile/Tree.html**

![screenshot](./image/module-list.jpg)

![screenshot](./image/dependencies.jpg)

**/build/reports/zen/analysis/library/compile/Tree.txt**

```
  7.741 MB	       0 B	\--- project :analysis-sample
  1.545 MB	    301 KB	 	+--- com.android.support:design:24.1.1
       0 B	  1.252 MB	 	|	+--- com.android.support:support-v4:24.1.1
       0 B	 21.275 KB	 	|	|	\--- com.android.support:support-annotations:24.1.1
    303 KB	    282 KB	 	|	+--- com.android.support:recyclerview-v7:24.1.1
       0 B	  1.252 MB	 	|	|	+--- com.android.support:support-v4:24.1.1
 21.275 KB	 21.275 KB	 	|	|	\--- com.android.support:support-annotations:24.1.1
    978 KB	    928 KB	 	|	\--- com.android.support:appcompat-v7:24.1.1
 49.837 KB	 11.365 KB	 	|	 	+--- com.android.support:animated-vector-drawable:24.1.1
 38.472 KB	 38.472 KB	 	|	 	|	\--- com.android.support:support-vector-drawable:24.1.1
       0 B	  1.252 MB	 	|	 	|	 	\--- com.android.support:support-v4:24.1.1
       0 B	  1.252 MB	 	|	 	+--- com.android.support:support-v4:24.1.1
 38.472 KB	 38.472 KB	 	|	 	\--- com.android.support:support-vector-drawable:24.1.1
    282 KB	    282 KB	 	+--- com.android.support:recyclerview-v7:24.1.1
 43.505 KB	 22.229 KB	 	+--- com.android.support:cardview-v7:24.1.1
 ...
```