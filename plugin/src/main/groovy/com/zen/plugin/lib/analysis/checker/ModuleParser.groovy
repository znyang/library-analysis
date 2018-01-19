package com.zen.plugin.lib.analysis.checker
/**
 * @author: zen. date: 2018/1/10 0010
 */
interface ModuleParser {

    boolean isRepeatPackage(String pkgName)

    String parseModuleName(String module, File aar)

}