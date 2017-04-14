package com.zen.plugin.lib.analysis.model

import com.zen.plugin.lib.analysis.ext.LibraryAnalysisExtension
import com.zen.plugin.lib.analysis.util.PackageChecker
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
    String id
    String name
    boolean open
    List<Node> children
    long fileSize
    long totalSize
    String iconSkin
    String detail

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
        return children != null && !children.isEmpty()
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
        node.setDetail(dep.getName())
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

    Set<String> supplyInfo(LibraryAnalysisExtension ext,
                           DependencyDictionary dictionary,
                           PackageChecker checker) {
        supplyInfo(null, ext, dictionary, checker);
    }

    /**
     * 将依赖树与依赖字典做匹配，计算出每个节点的文件大小总和
     *
     * TODO: 性能不佳，数据/视图未作分离
     *
     * @param dependencies 存放每个依赖节点的子节点集合信息
     * @param ext
     * @param dictionary
     * @param isParentIgnore
     * @return 当前+子节点的dependency id集合。如果节点被忽略，返回空
     */
    Set<String> supplyInfo(Map<String, Set<String>> dependencies,
                           LibraryAnalysisExtension ext,
                           DependencyDictionary dictionary,
                           PackageChecker checker,
                           boolean isParentIgnore = false) {
        if (!dependencies) {
            dependencies = new HashMap<>()
        }

        def ids = new HashSet<String>()
        def info = dictionary.findDependencyInfo(id)
        def ignore = isParentIgnore | ext.isIgnore(id)
        def idsInDic = dependencies.get(id)

        ids.add(id)
        children?.each {
            def result = it.supplyInfo(dependencies, ext, dictionary, checker, ignore)
            if (idsInDic) {
                ids.addAll(idsInDic)
            } else {
                ids.addAll(result)
            }
        }
        if (ignore) {
            ids.clear()
        }

        if (!idsInDic) {
            dependencies.put(id, ids)
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
        if (info) {
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

        // Android package name
        def packageName = null
        if (info?.type?.equals('aar') && checker) {
            packageName = "<span class='tag' style='color:#999'>${checker.parseModuleName(info.id, info.file)}</span>"
        }

        // 累计依赖库大小 + 当前依赖库大小 + 依赖库名称
        name = "${txtTotalSize}${txtSize ?: ''} ${detail} ${packageName ?: ''}"

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
