package com.zen.plugin.lib.analysis.render

import com.google.gson.Gson
import com.zen.plugin.lib.analysis.model.Node
import com.zen.plugin.lib.analysis.util.ResourceUtils

/**
 * @author zen
 * @version 2016/9/9
 */

class HtmlRenderer {

    private static final Gson GSON = new Gson()

    private String targetDir

    HtmlRenderer(String target) {
        this.targetDir = target
    }

    public String render(Node root) {
        String json = root ? "[${GSON.toJson(root)}]" : '[]'
        File target = new File(targetDir, "Tree.html")
        String html = ResourceUtils.getTemplateFileContent("Tree.html")
                .replace("%title%", root.getName())
                .replace("%nodes%", json)
        target.setText(html, "UTF-8")

        target.path
    }

}