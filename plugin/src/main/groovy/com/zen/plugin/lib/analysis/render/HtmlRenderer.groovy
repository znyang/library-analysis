package com.zen.plugin.lib.analysis.render

import com.google.gson.Gson
import com.zen.plugin.lib.analysis.model.Node
import com.zen.plugin.lib.analysis.util.ResourceUtils
import groovy.json.JsonOutput

/**
 * @author zen
 * @version 2016/9/9
 */

class HtmlRenderer {

    private static final GSON = new Gson()

    private String targetDir

    HtmlRenderer(target) {
        this.targetDir = target
    }

    public String render(Node root, OutputModuleList list, String msg) {
        String json = root ? "[${GSON.toJson(root)}]" : '[]'
        if (msg && msg.length() > 0) {
            msg = msg.replace("\r\n", "<br>")
        } else {
            msg = ""
        }

        def modules = JsonOutput.toJson(list)

        def target = new File(targetDir, "Tree.html")
        def html = ResourceUtils.getTemplateFileContent("Tree.html")
                .replace("%output_msg%", msg)
                .replace("%data%", modules)
                .replace("%title%", root.getName())
                .replace("%nodes%", json)
        target.setText(html, "UTF-8")

        target.path
    }

}