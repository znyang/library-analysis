package com.zen.plugin.lib.analysis.model

import com.zen.plugin.lib.analysis.ext.LibraryAnalysisExtension
import com.zen.plugin.lib.analysis.mock.DependencyGenerator
import org.gmock.GMockTestCase
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableDependency

/**
 * @author zen
 * @version 2016/9/10
 */

class NodeTest extends GMockTestCase {

    RenderableDependency data
    DependencyDictionary dictionary
    DependencyGenerator  generator
    LibraryAnalysisExtension extension

    @Override
    void setUp() {
        super.setUp()
        generator = new DependencyGenerator(gMockController)
        data = generator.create()

        def fileCollection = generator.getFileCollection()
        dictionary = new DependencyDictionary(fileCollection)
        extension = new LibraryAnalysisExtension()
    }

    void testData() {
        play {
            Node node = Node.create(data)
            node.supplyInfo(extension, dictionary)

            node.children.each {
                checkFileSize(it)
            }
        }
    }

    void checkFileSize(Node node) {
        assert node.fileSize > 0L
        if (node.children != null) {
            node.children.each {
                checkFileSize(it)
            }
        }
    }
}
