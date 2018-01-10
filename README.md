# Library Analysis Gradle Plugin

[![Build Status](https://travis-ci.org/znyang/library-analysis.svg?branch=master)](https://travis-ci.org/znyang/library-analysis)
[![](https://jitpack.io/v/znyang/library-analysis.svg)](https://jitpack.io/#znyang/library-analysis)
[![codecov.io](https://codecov.io/github/znyang/library-analysis/coverage.svg?branch=master)](https://codecov.io/gh/znyang/library-analysis/branch/master)

## 概述

分析各依赖库文件的大小

1. 支持大文件提醒
2. 支持忽略部分依赖库大小（大小显示为灰色）
3. 支持依赖节点大小统计
4. 输出aar的PackageName以及冲突提示
5. 输出依赖库被直接依赖次数(Used)、包含的依赖库数量(Contains)
6. 标识可移除的依赖库（实验性功能）

>可移除的依赖库<br>
例如有这样的依赖关系：A->B->C, A->C，那么A~~->C~~

## 配置

```gradle
buildscript {
    repositories {
        // ...
        maven { url "https://jitpack.io" }
    }
    dependencies {
        classpath 'com.github.znyang:library-analysis:0.2.1'
    }
}

apply plugin: 'com.zen.lib.analysis'

libReport {
    output = [
        "txt", "html" // default
    ]
    ignore = [
        "com.android.support:support-v4"
    ]
}
```

## 使用

```
gradle libReportCompile // or libReportReleaseCompileClasspath ...
```

注意：在使用gradle 4.x以后，建议使用libReportReleaseCompileClasspath输出（libReportCompile无法输出使用implementation加入的相关依赖库），如果工程都只用compile添加依赖，那么没有影响。

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
