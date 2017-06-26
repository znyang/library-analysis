package com.zen.plugin.lib.analysis.model

import com.zen.plugin.lib.analysis.ext.LibraryAnalysisExtension
import com.zen.plugin.lib.analysis.mock.DependencyGenerator
import com.zen.plugin.lib.analysis.util.Logger
import org.gmock.GMockTestCase
import org.junit.Before
import org.junit.Test

/**
 * @author zen
 * @version 2016/9/10
 */

class NodeTest extends GMockTestCase {

    def root
    def dictionary
    def generator
    def extension
    def fileCollection

    @Override
    @Before
    void setUp() {
        super.setUp()
        extension = new LibraryAnalysisExtension()
        generator = new DependencyGenerator(gMockController)
        root = generator.mockRootDependency()
        fileCollection = generator.mockFileCollection()
    }

    private void prePlay() {
        dictionary = new FileDictionary(fileCollection)
    }

    @Test
    void testData() {
        play {
            prePlay()

            Node node = Node.create(root)
            node.supplyInfo(extension, dictionary, null)

            node.children.each {
                checkFileSize(it)
            }
        }
    }

    void checkFileSize(Node node) {
        String data = node.toString()
        Logger.D?.log data

        assert node.fileSize > 0L
        if (node.children != null) {
            node.children.each {
                checkFileSize(it)
            }
        }
    }
}
