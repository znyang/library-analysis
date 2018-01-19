package com.zen.plugin.lib.analysis.checker

import com.zen.plugin.lib.analysis.model.Library

import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * @author: zen. date: 2018/1/10 0010
 */
abstract class ErrorChecker {

    boolean isTarget(Library lib, File zip) {
        true
    }

    abstract void onAnalysisFile(Library lib, ZipFile file, ZipEntry entry)

    abstract String outputReport()

}