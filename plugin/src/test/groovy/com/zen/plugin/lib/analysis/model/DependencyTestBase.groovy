package com.zen.plugin.lib.analysis.model

import com.zen.plugin.lib.analysis.mock.DependencyGenerator
import org.gmock.GMockController
import org.gmock.GMockTestCase
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableDependency

/**
 *
 * @author: zen
 * date: 2017/6/26 0026.
 */
class DependencyTestBase extends GMockTestCase {

    RenderableDependency root
    FileDictionary fileDic
    def files

    @Override
    void setUp() {
        super.setUp()
        def generator = new DependencyGenerator(gMockController as GMockController)
        root = generator.mockRootDependency()
        files = generator.mockFileCollection()
    }

    void prePlay() {
        fileDic = new FileDictionary(files)
    }

    void printLibrary(Library lib, String deep) {
        println "【U=${lib.useCount}】【C=${lib.contains.size()}】${deep}${lib.name} - ${lib.useCountImmediate}"
        lib.children?.each {
            printLibrary(it, deep + "  ")
        }
    }

    void printNode(Node lib, String deep) {
        println "【C=${lib.getChildrenSize()}】${deep}${lib.name} - 重复？ ${lib.canRemove}"
        lib.children?.each {
            printNode(it, deep + "  ")
        }
    }

    void testEmpty() {

    }
}
