package com.zen.plugin.lib.analysis.model

import com.zen.plugin.lib.analysis.ext.LibraryAnalysisExtension
import com.zen.plugin.lib.analysis.util.FileUtils
import com.zen.plugin.lib.analysis.util.Logger
import com.zen.plugin.lib.analysis.util.Timer
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

    static Map<String, Set<String>> DEP_DIC = new HashMap<>()

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
        DEP_DIC.clear()
        supplyInfo(ext, dictionary, false)
    }

    /**
     * TODO: 性能不佳，数据/视图未作分离
     *
     * @param ext
     * @param dictionary
     * @param isParentIgnore
     * @return
     */
    Set<String> supplyInfo(LibraryAnalysisExtension ext, DependencyDictionary dictionary, boolean isParentIgnore) {
        def ids = new HashSet<String>()
        def info = dictionary.findDependencyInfo(id)
        def ignore = isParentIgnore | ext.isIgnore(id)
        def hasInDic = DEP_DIC.containsKey(id)
        def idsInDic = DEP_DIC.get(id)

        ids.add(id)
        children?.each {
            def result = it.supplyInfo(ext, dictionary, ignore)
            if (hasInDic) {
                ids.addAll(idsInDic)
            } else {
                ids.addAll(result)
            }
        }
        if (ignore) {
            ids.clear()
        }

        if (!hasInDic) {
            DEP_DIC.put(id, ids)
        } else {
            ignore |= idsInDic.isEmpty()
            ids = idsInDic
        }

        long size = 0
        Logger.D?.log "find ids for ${id}"
        ids.each {
            def data = dictionary.findDependencyInfo(it)
            if (data != null) {
                size += data.size
                Logger.D?.log "find ${it} ${data.size}"
            } else {
                Logger.D?.log "not found ${it}"
            }
        }
        Logger.D?.log "find result ${id} = ${size}"
        totalSize = size

        def txtSize = null
        def txtType = null
        if (info != null) {
            def styleSize = ignore ? "tag-ignore" : ext.getSizeTag(info.size)
            def styleType = "jar".equals(info.type) ? 'type_jar' : 'type_aar'

            txtSize = "<span class='tag ${styleSize}'>${FileUtils.convertFileSize(info.size)}</span>"
            txtType = "<span class='button ${styleType}'></span>"

            fileSize = info.size
        }

        def styleTotalSize = ignore ? "tag-ignore" : ext.getSizeTag(totalSize)
        def txtTotalSize = "<span class='tag ${styleTotalSize}'>${FileUtils.convertFileSize(totalSize)}</span>"
        name = "${txtType ?: ""}${txtTotalSize}${txtSize ?: ""}${name}"

        ids
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
