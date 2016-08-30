package com.zen.plugin.lib.analysis.conf;

/**
 * StringConstants
 *
 * @author yangz
 * @version 2016/7/8
 */
public class StringConstants {
    public static final String CONTENT_FORMAT = "<!DOCTYPE html>\n" +
            "<HTML>\n" +
            " <HEAD>\n" +
            "  <TITLE> %s Dependencies </TITLE>\n" +
            "  <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n" +
            "  <link rel=\"stylesheet\" href=\"css/demo.css\" type=\"text/css\">\n" +
            "  <link rel=\"stylesheet\" href=\"css/zTreeStyle/zTreeStyle.css\" type=\"text/css\">\n" +
            "  <script type=\"text/javascript\" src=\"js/jquery-1.4.4.min.js\"></script>\n" +
            "  <script type=\"text/javascript\" src=\"js/jquery.ztree.core.min.js\"></script>\n" +
            "  <SCRIPT LANGUAGE=\"JavaScript\">\n" +
            "   var zTreeObj;\n" +
            "   var setting = {};\n" +
            "   var zNodes = %s;\n" +
            "   $(document).ready(function(){\n" +
            "      zTreeObj = $.fn.zTree.init($(\"#treeDemo\"), setting, zNodes);\n" +
            "   });\n" +
            "  </SCRIPT>\n" +
            " </HEAD>\n" +
            "<BODY>\n" +
            "<div>\n" +
            "   <p class=\"ztree\">注：+ 表示该依赖库有子节点，但是这个库已经出现过了，为了节省数据量省略掉子节点。</p>" +
            "   <ul id=\"treeDemo\" class=\"ztree\"></ul>\n" +
            "</div>\n" +
            "</BODY>\n" +
            "</HTML>";

    public static String getGraphFormat(String name, String nodes, String edges) {
        final String data = "nodes: " + nodes + ", edges: " + edges;
        // String.format("nodes: %s,\nedges: %s\n", nodes,edges);
        return GRAPH_HTML1 + data + String.format(GRAPH_HTML2, name);
    }

    public static final String GRAPH_HTML1 = "<!DOCTYPE>\n" +
            "\n" +
            "<html>\n" +
            "\n" +
            "<head>\n" +
            "<title> Dependencies DAG </title>\n" +
            "\n" +
            "<meta name=\"viewport\" content=\"width=device-width, user-scalable=no, initial-scale=1, maximum-scale=1\">\n" +
            "\n" +
            "<script src=\"http://cdn.bootcss.com/jquery/2.2.4/jquery.min.js\"></script>\n" +
            "<script src=\"http://cdn.bootcss.com/cytoscape/2.7.7/cytoscape.min.js\"></script>\n" +
            "\n" +
            "<!-- for testing with local version of cytoscape.js -->\n" +
            "<!--<script src=\"../cytoscape.js/build/cytoscape.js\"></script>-->\n" +
            "\n" +
            "<script src=\"https://cdn.rawgit.com/cpettitt/dagre/v0.7.4/dist/dagre.min.js\"></script>\n" +
            "<script src=\"https://cdn.rawgit.com/cytoscape/cytoscape.js-dagre/1.1.2/cytoscape-dagre.js\"></script>\n" +
            "\n" +
            "<style>\n" +
            "body {\n" +
            "font-family: helvetica;\n" +
            "font-size: 14px;\n" +
            "}\n" +
            "\n" +
            "#cy {\n" +
            "width: 100%;\n" +
            "height: 100%;\n" +
            "position: absolute;\n" +
            "left: 0;\n" +
            "top: 0;\n" +
            "z-index: 999;\n" +
            "}\n" +
            "\n" +
            "h1 {\n" +
            "opacity: 0.5;\n" +
            "font-size: 1em;\n" +
            "}\n" +
            "</style>\n" +
            "\n" +
            "<script>\n" +
            "$(function(){\n" +
            "\n" +
            "var cy = window.cy = cytoscape({\n" +
            "container: document.getElementById('cy'),\n" +
            "\n" +
            "          boxSelectionEnabled: false,\n" +
            "          autounselectify: true,\n" +
            "\n" +
            "layout: {\n" +
            "name: 'dagre'\n" +
            "},\n" +
            "\n" +
            "style: [\n" +
            "{\n" +
            "selector: 'node',\n" +
            "style: {\n" +
            "        'content': 'data(name)',\n" +
            "        'text-halign': 'right',\n"+
            "        'text-valign': 'center',\n" +
            "        'color': 'white',\n" +
            "        'text-outline-width': 2,\n" +
            "        'background-color': '#669',\n" +
            "        'text-outline-color': '#669'" +
            "}\n" +
            "},\n" +
            "\n" +
            "{\n" +
            "selector: 'edge',\n" +
            "style: {\n" +
            "        'curve-style': 'bezier',\n" +
            "        'target-arrow-shape': 'triangle',\n" +
            "        'target-arrow-color': '#ccc',\n" +
            "        'line-color': '#ccc',\n" +
            "        'width': 3" +
            "}\n" +
            "}\n" +
            "],\n" +
            "\n" +
            "elements: {\n";
    public static final String GRAPH_HTML2 = "},\n" +
            "});\n" +
            "\n" +
            "});\n" +
            "</script>\n" +
            "</head>\n" +
            "\n" +
            "<body>\n" +
            "<h1>%s</h1>\n" +
            "\n" +
            "<div id=\"cy\"></div>\n" +
            "\n" +
            "</body>\n" +
            "\n" +
            "</html>";
}
