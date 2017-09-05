package com.zen.plugin.lib.analysis.model
/**
 *
 * @author: zen
 * date: 2017/6/26 0026.
 */
class LibraryTest extends DependencyTestBase {

    void testCreate() {
        play {
            prePlay()

            long start = System.currentTimeMillis()
            Library output = Library.create(root, fileDic)
            long cost = System.currentTimeMillis() - start

            System.out.println("${cost}ms")
            assertTrue(cost < 500)

            assertEquals(root.id, output.id)
            assertEquals(root.name, output.name)
            assertEquals(output.children.size(), root.children.size())
            assertTrue(output.children.size() > 0)

            printLibrary(output, "")
        }
    }

    void testIgnore() {
        play {
            prePlay()

            long start = System.currentTimeMillis()
            Library output = Library.create(root, fileDic)
            output.applyIgnoreLibrary("com.android.support:support-v4", "com.android.support")
            long cost = System.currentTimeMillis() - start

            System.out.println("${cost}ms")
            assertTrue(cost < 500)

            printLibrary(output, "")
            assertEquals(output.getTotalSizeWithoutIgnore(), 0)
            assertTrue(output.getTotalSize() > 0)
        }
    }

}
