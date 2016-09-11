package com.zen.plugin.lib.analysis.model

import com.zen.plugin.lib.analysis.ext.LibraryAnalysisExtension
import com.zen.plugin.lib.analysis.util.FileUtils
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableDependency

/**
 * Node
 *
 * @author yangz
 * @version 2016/7/8
 */
class Node {
    String     id
    String     name
    boolean    open
    List<Node> children
    long       fileSize
    long       totalSize
    String     iconSkin

    void addNode(Node node) {
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

    boolean hasChildren() {
        return children == null || children.isEmpty();
    }

    static Node create(RenderableDependency dep) {
        create(dep, new HashSet<>())
    }

    static Node create(RenderableDependency dep, Set<Object> collection) {
        if (dep == null) {
            return null
        }

        // 是否已经加入过
        boolean hasAdded = collection.contains(dep.getId())
        Set<RenderableDependency> children = dep.getChildren()
        boolean hasChildren = !children?.isEmpty();
        String id = dep.getId().toString()

        Node node = new Node()
        node.setId(id)
        node.setName(dep.getName())
        if (hasAdded && hasChildren) {
            node.iconSkin = "omit"
        }

        if (!hasAdded) {
            if (collection != null) {
                collection.add(dep.getId())
            }

            if (hasChildren) {
                children.each {
                    node.addNode(create(it, collection))
                }
            }
        }
        node.open = !hasAdded
        node
    }

    void supplyInfo(LibraryAnalysisExtension ext, DependencyDictionary dictionary) {
        supplyInfo(ext, dictionary, false)
    }

    void supplyInfo(LibraryAnalysisExtension ext, DependencyDictionary dictionary, boolean isParentIgnore) {
        def info = dictionary.findDependencyInfo(id)
        def ignore = isParentIgnore | ext.isIgnore(id)
        if (info != null) {
            if (info != null) {
                def styleSize = ignore ? "tag-ignore" : ext.getSizeTag(info.size)
                def styleType = "jar".equals(info.type) ? 'type_jar' : 'type_aar'

                def txtSize = "<span class='tag ${styleSize}'> ${FileUtils.convertFileSize(info.size)} </span>"
                def txtType = "<span class='button ${styleType}'></span>"

                fileSize = info.size
                name = "${txtType}${txtSize}${name}"
            }
        }

        children?.each {
            it.supplyInfo(ext, dictionary, ignore)
        }
    }


    @Override
    public String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", open=" + open +
                ", children=" + children +
                ", fileSize=" + fileSize +
                ", totalSize=" + totalSize +
                ", iconSkin='" + iconSkin + '\'' +
                '}';
    }
}
