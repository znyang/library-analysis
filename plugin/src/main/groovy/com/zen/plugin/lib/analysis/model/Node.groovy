package com.zen.plugin.lib.analysis.model
/**
 * Node
 *
 * 依赖节点的输出模型
 *
 * @author yangz
 * @version 2016/7/8
 */
class Node {
    String id
    String name
    boolean open
    List<Node> children
    long fileSize
    long totalSize
    String iconSkin
    String detail
    // 可移除的依赖库（重复添加）
    boolean canRemove

    void addNode(Node node) {
        if (node == null) {
            return
        }
        List<Node> children = getChildren()
        if (children == null) {
            children = new ArrayList<>()
            setChildren(children)
        }
        children.add(node)
    }

    int getChildrenSize() {
        children == null ? 0 : children.size()
    }

    boolean hasChildren() {
        return children != null && !children.isEmpty()
    }

    @Override
    String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", open=" + open +
                ", children=" + children +
                ", fileSize=" + fileSize +
                ", totalSize=" + totalSize +
                ", iconSkin='" + iconSkin + '\'' +
                '}'
    }
}
