package com.zen.plugin.lib.analysis.render

import com.zen.plugin.lib.analysis.model.Node
import com.zen.plugin.lib.analysis.util.FileUtils

/**
 * @author: zen
 * date: 2017/4/13 0013.
 */
public class TextRenderer {


    private String targetDir

    TextRenderer(def target) {
        this.targetDir = target
    }

    public String render(Node root, OutputModuleList list, String msg) {
        if (msg && msg.length() > 0) {
            msg = msg.replace("\r\n", "<br>")
        } else {
            msg = ""
        }

        def target = new File(targetDir, "Tree.txt")
        StringBuilder builder = new StringBuilder()
        builder.append(msg).append("\r\n")

        renderNode(builder, root, "")

        target.setText(builder.toString(), "UTF-8")
        target.path
    }

    private String renderNode(StringBuilder builder, Node node, String prev) {
        if (prev && !prev.isEmpty()) {
            builder.append(prev)
        }
        builder.append(node.isOpen() ? "+" : "-")
                .append("${node.id}\t${FileUtils.convertFileSize(node.fileSize)}/${FileUtils.convertFileSize(node.totalSize)}")
                .append("\r\n")
        if (node.hasChildren()) {
            for (Node child : node.children) {
                renderNode(builder, child, "${prev}\t")
            }
        }
    }

}
