package com.zen.plugin.lib.analysis.convert

import com.zen.plugin.lib.analysis.ext.LibraryAnalysisExtension
import com.zen.plugin.lib.analysis.model.FileDictionary
import com.zen.plugin.lib.analysis.model.FileInfo
import com.zen.plugin.lib.analysis.model.Library
import com.zen.plugin.lib.analysis.model.Node
import com.zen.plugin.lib.analysis.util.FileUtils
import com.zen.plugin.lib.analysis.util.PackageChecker

/**
 *
 * @author: zen
 * date: 2017/6/26 0026.
 */
class NodeConvert {

    static class ConvertArgs {
        FileDictionary fileDictionary
        LibraryAnalysisExtension ext
        PackageChecker checker

        static ConvertArgs with(FileDictionary fileDictionary, LibraryAnalysisExtension ext, PackageChecker checker) {
            def args = new ConvertArgs()
            args.fileDictionary = fileDictionary
            args.ext = ext
            args.checker = checker
            args
        }
    }

    static Node convert(Library lib, ConvertArgs args, Set<Object> cacheIds = new HashSet<>()) {
        boolean hasAdded = cacheIds.contains(lib.id)
        boolean hasChildren = !lib.children.isEmpty()

        Node node = new Node()
        node.id = lib.id
        node.detail = lib.name
        if (hasAdded && !lib.children.isEmpty()) {
            node.iconSkin = "omit" // 表示简略的依赖库集合
        }

        if (!hasAdded) {
            cacheIds?.add(lib.id)

            if (hasChildren) {
                lib.children.each {
                    node.addNode(convert(it, args, cacheIds))
                }
            }
        }
        node.open = !hasAdded
        node.totalSize = lib.getTotalSizeWithoutIgnore()

        // 存在依赖信息
        FileInfo info = lib.file

        def (txtTotalSize, txtSize, txtType) = outputTxt(info, lib, args, node)

        // Android package name
        def packageName = parsePackageName(info, args)

        // 累计依赖库大小 + 当前依赖库大小 + 依赖库名称
        node.name = "${txtTotalSize}${txtSize ?: ''} ${node.detail} ${packageName ?: ''}"

        node
    }

    private static Object parsePackageName(FileInfo info, ConvertArgs args) {
        def packageName = null
        if (info?.type == 'aar' && args.checker) {
            packageName = "<span class='tag' style='color:#999'>${args.checker.parseModuleName(info.id, info.file)}</span>"
        }
        packageName
    }

    private static List outputTxt(FileInfo info, Library lib, ConvertArgs args, Node node) {
        def txtType = null
        def txtSize = null

        if (info) {
            def styleSize = lib.isIgnoreSize ? "tag-ignore" : (args.ext ? args.ext.getSizeTag(info.size) : 'tag-normal')
            def styleType = "jar" == info.type ? 'type_jar' : 'type_aar'

            txtType = "<span class='type ${styleType}'></span>"
            txtSize = "<span class='tag ${styleSize}'>${txtType}${FileUtils.convertFileSize(info.size)}</span>"

            node.fileSize = info.size
        }

        def styleTotalSize = "class='tag tag-ignore'"
        if (!lib.isIgnoreSize) {
            styleTotalSize = "class='tag' style='color:#fff;background-color:${genColorCode(node.totalSize, args.fileDictionary.totalSize)}'"
        }
        def txtTotalSize = "<span ${styleTotalSize}>${FileUtils.convertFileSize(node.totalSize)}</span>"
        [txtTotalSize, txtSize, txtType]
    }

    private static String genColorCode(long size, long max) {
        if (max == 0L) {
            return "#ffdddd"
        }
        final int color = 0xdd - 0x22
        def c = max - size * 4
        if (c < 0) {
            c = 0
        }

        int result = color * c / max
        result += (result << 8) + 0xff0000
        "#${Integer.toHexString(result)}"
    }

}
