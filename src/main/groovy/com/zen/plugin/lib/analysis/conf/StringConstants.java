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
            "   // zTree 的参数配置，深入使用请参考 API 文档（setting 配置详解）\n" +
            "   var setting = {};\n" +
            "   // zTree 的数据属性，深入使用请参考 API 文档（zTreeNode 节点数据详解）\n" +
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
}
