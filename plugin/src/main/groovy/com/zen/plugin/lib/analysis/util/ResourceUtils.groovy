package com.zen.plugin.lib.analysis.util;

/**
 * @author zen
 * @version 2016/9/9
 */

class ResourceUtils {

    static final RESOURCE_PATH = "/com/zen/plugin/lib/analysis/"
    static final RESOURCE_FILES = [
            "css/z/img/",
            "css/demo.css",
            "css/z/ztree.css",
            "css/z/img/line_conn.gif",
            "css/z/img/loading.gif",
            "css/z/img/zTreeStandard.gif",
            "css/z/img/zTreeStandard.png",
            "css/z/img/jar.png",
            "css/z/img/aar.png",

            "js/",
            "js/jquery.ztree.core.min.js",
            "js/jquery-1.4.4.min.js",
            "js/cytoscape-dagre.js",
            "js/dagre.min.js"
    ]

    private static obj = new ResourceUtils()

    private ResourceUtils() {
    }

    static void copyResources(String targetPath) {
        RESOURCE_FILES.each {
            if (it.endsWith('/')) {
                new File(targetPath, it).mkdirs()
                return
            }

            def target = new File(targetPath, it)
            if (!target.exists()) {
                def source = obj.getClass().getResourceAsStream("${RESOURCE_PATH}${it}")
                target.withDataOutputStream {
                    os -> os << source
                }
            }
        }
    }

    static String getTemplateFileContent(String fileName) {
        return obj.getClass().getResourceAsStream("${RESOURCE_PATH}${fileName}").getText("UTF-8")
    }

}
