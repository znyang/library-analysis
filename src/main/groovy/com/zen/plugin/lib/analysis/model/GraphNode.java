package com.zen.plugin.lib.analysis.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GraphNode
 *
 * @author yangz
 * @version 2016/8/4
 */
public class GraphNode {
    public  String                 name;
    public  List<GraphNode>        children;
    private Map<String, GraphNode> dag;

    public GraphNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, GraphNode> getDag() {
        return dag;
    }

    public void setDag(Map<String, GraphNode> dag) {
        this.dag = dag;
    }

    public List<GraphNode> getChildren() {
        return children;
    }

    public void setChildren(List<GraphNode> children) {
        this.children = children;
    }

    public void addChild(GraphNode node) {
        if (node == null) {
            return;
        }
        List<GraphNode> children = getChildren();
        if (children == null) {
            children = new ArrayList<>();
            setChildren(children);
        }
        children.add(node);
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GraphNode graphNode = (GraphNode) o;

        return name != null ? name.equals(graphNode.name) : graphNode.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
