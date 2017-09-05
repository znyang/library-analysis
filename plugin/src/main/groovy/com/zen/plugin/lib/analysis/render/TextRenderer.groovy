package com.zen.plugin.lib.analysis.render

import com.zen.plugin.lib.analysis.model.Node
import com.zen.plugin.lib.analysis.util.FileUtils

/**
 * @author: zen
 * date: 2017/4/13 0013.
 */
class TextRenderer {


    private String targetDir

    TextRenderer(def target) {
        this.targetDir = target
    }

    String render(Node root, OutputModuleList list, String msg) {
        if (msg && msg.length() > 0) {
            msg = msg.replace("\r\n", "<br>")
        } else {
            msg = ""
        }

        def target = new File(targetDir, "Tree.txt")
        StringBuilder builder = new StringBuilder()
        builder.append(msg).append("\r\n")

        renderNode(builder, root, "", true)
        showAllModules(builder, list)

        target.setText(builder.toString(), "UTF-8")
        target.path
    }

    private String renderNode(StringBuilder builder, Node node, String prev, boolean isLast) {
        def fileSize = FileUtils.convertFileSize(node.fileSize)
        def totalSize = FileUtils.convertFileSize(node.totalSize)
        builder.append("${textAlign(totalSize, 10)}\t${textAlign(fileSize, 10)}\t")

        if (prev && !prev.isEmpty()) {
            builder.append(prev)
        }
        boolean hasChildren = node.hasChildren()
        def mark = "+---"
        if (isLast) {
            mark = "\\---"
        }

        builder.append(mark).append(" ${node.detail}").append("\r\n")
        if (hasChildren) {
            List<Node> children = node.children
            int size = children.size()
            prev = isLast ? "${prev} \t" : "${prev}|\t"
            for (int i = 0; i < size - 1; i++) {
                renderNode(builder, children.get(i), prev, false)
            }
            renderNode(builder, children.get(size - 1), prev, true)
        }
    }

    static String textAlign(String content, int count) {
        if (count == 0) {
            return content
        }
        if (content != null && content.length() > count) {
            return content
        }

        char[] chrs = new char[count]
        for (int i = count; --i >= 0;) {
            chrs[i] = ' '
        }
        content.getChars(0, content.size(), chrs, count - content.size())
        return String.valueOf(chrs)
    }

    static void showAllModules(StringBuilder builder, OutputModuleList list) {
        builder.append("\r\n")
        list.modules.each {
            builder.append('compile \'').append(it.name).append('@').append(it.type).append('\'').append("\r\n")
        }
    }

}
