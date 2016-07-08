package com.zen.plugin.lib.analysis.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Node
 *
 * @author yangz
 * @version 2016/7/8
 */
public class Node {
    public String name;
    public boolean open;
    public List<Node> children;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public void addNode(Node node) {
        if (node == null) {
            return;
        }
        List<Node> children = getChildren();
        if (children == null) {
            children = new ArrayList<>();
            setChildren(children);
        }
        children.add(node);
    }

    public boolean hasChildren() {
        return children == null || children.isEmpty();
    }

}
