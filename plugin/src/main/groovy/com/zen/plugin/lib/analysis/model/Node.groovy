package com.zen.plugin.lib.analysis.model

import com.zen.plugin.lib.analysis.ext.LibraryAnalysisExtension
import com.zen.plugin.lib.analysis.util.FileUtils
import com.zen.plugin.lib.analysis.util.Logger
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
    Set<String> supplyInfo(LibraryAnalysisExtension ext,
                           DependencyDictionary dictionary,
                           boolean isParentIgnore) {
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

            txtType = "<span class='type ${styleType}'></span>"
            txtSize = "<span class='tag ${styleSize}'>${txtType}${FileUtils.convertFileSize(info.size)}</span>"

            fileSize = info.size
        }

//        def styleTotalSize = ignore ? "tag-ignore" : ext.getSizeTag(totalSize)
        def styleTotalSize = "class='tag tag-ignore'"
        if (!ignore) {
            styleTotalSize = "class='tag' style='color:#fff;background-color:${genColorCode(totalSize, dictionary.totalSize, dictionary.getFiles().size())}'"
        }
        def txtTotalSize = "<span ${styleTotalSize}>${FileUtils.convertFileSize(totalSize)}</span>"
        name = "${txtTotalSize}${txtSize ?: ''}${name}"

        ids
    }

    private static String genColorCode(long size, long max, int libSize) {
        if (max == 0L) {
            return "#ffdddd"
        }
        final int color = 0xdd - 0x22
        def c = max - size * 4
        if (c < 0) {
            c = 0
        }

        int result = color * c / max
        result += (result << 8) + 0xff0000
        "#${Integer.toHexString(result)}"
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
