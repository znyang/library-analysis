package com.zen.plugin.lib.analysis.renderer

import com.zen.plugin.lib.analysis.VariantAnalysisHelper
import com.zen.plugin.lib.analysis.conf.StringConstants
import com.zen.plugin.lib.analysis.model.GraphNode
import com.zen.plugin.lib.analysis.model.Node
import org.gradle.api.tasks.diagnostics.internal.TextReportRenderer

import java.nio.file.Files

/**
 * LibraryHtmlReportRenderer
 *
 * @author yangz
 * @version 2016/7/8
 */
class LibraryGraphHtmlReportRenderer extends TextReportRenderer {

    public void render(final Node root) throws IOException {
        String json = "[]";
        if (root != null) {
            GraphNode graphNode = VariantAnalysisHelper.convertGraphNode(root);
            String nodes = getNodesData(graphNode)
            String edges = getEdgesData(graphNode)
//            System.out.println(nodes)
//            System.out.println(edges)
            getTextOutput().text(StringConstants.getGraphFormat(root.getName(), nodes, edges));
        } else {
            getTextOutput().text(StringConstants.getGraphFormat("No Dependencies", json, json));
        }
    }

    private void copyFile2() {
        final String target = "build/gen/demo.css";
        new File("build/gen/").mkdirs()
//        final File src = new File(getClass().getClassLoader().getResource("./").getPath());
//
//        src.eachFile {
//            println it.getAbsoluteFile()
//        }
        def source = this.getClass().getResourceAsStream("/com/zen/plugin/lib/analysis/css/demo.css")
        new File(target).withDataOutputStream {
            os ->
//                new File(source).withDataInputStream {
                os << source
//                }
        }
    }

    private static String getNodesData(GraphNode graphNode) {
        Map<String, GraphNode> dag = graphNode.getDag();
        StringBuilder nodes = new StringBuilder()
        nodes.append('[')
        dag.each {
            key, value ->
                String format = "{ data: { id: '%s', name: '%s' } },\n";
                nodes.append(String.format(format, value.getName(), value.getName()))
        }
        nodes.append(']')
        return nodes.toString()
    }

    private static String getEdgesData(GraphNode graphNode) {
        Map<String, GraphNode> dag = graphNode.getDag();
        StringBuilder edges = new StringBuilder()
        edges.append('[')

        dag.each {
            key, value ->
                if (value.hasChildren()) {
                    def children = value.getChildren()
                    for (GraphNode child : children) {
                        edges.append("{ data: { source: '")
                                .append(value.getName())
                                .append("', target: '")
                                .append(child.getName())
                                .append("' } },\n")
                    }
                }
        }
        edges.append(']')
        return edges.toString()
    }

}
