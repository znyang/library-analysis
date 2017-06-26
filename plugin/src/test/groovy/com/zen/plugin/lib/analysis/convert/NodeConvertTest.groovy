package com.zen.plugin.lib.analysis.convert

import com.zen.plugin.lib.analysis.model.DependencyTestBase
import com.zen.plugin.lib.analysis.model.Library

/**
 *
 * @author: zen
 * date: 2017/6/26 0026.
 */
class NodeConvertTest extends DependencyTestBase {

    void testConvert() {
        play {
            prePlay()

            long start = System.currentTimeMillis()
            Library output = Library.create(root, fileDic)
//            def node = NodeConvert.convert(output, NodeConvert.ConvertArgs.with(fileDic, null, new PackageChecker()))
            long cost = System.currentTimeMillis() - start

            System.out.println("${cost}ms")
            assertTrue(cost < 500)

            printLibrary(output, "")
//            printNode(node, "")
        }
    }

}
