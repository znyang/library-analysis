package com.zen.plugin.lib.analysis.model

import org.gmock.GMockTestCase
import org.gradle.api.file.FileCollection
import org.junit.Before
import org.junit.Test

/**
 * @author zen
 * @version 2016/9/10
 */

class DependencyDictionaryTest extends GMockTestCase {

    static final FILE_INFO = [
            [
                    'id'  : "com.android.support:appcompat-v7:22.2",
                    'name': "appcompat-v7-22.2.0.aar",
                    'path': "Z:\\sdk\\Android\\extras\\android\\m2repository\\com\\android\\support\\appcompat-v7\\22.2.0\\appcompat-v7-22.2.0.aar",
                    'size': 10 * 1024
            ],
            [
                    'id'  : "project :rx-hermes",
                    'name': "rx-hermes-release.aar",
                    'path': "Z:\\projects\\coding\\twinkle\\hermes-rx\\rx-hermes\\build\\outputs\\aar\\rx-hermes-release.aar",
                    'size': 500 * 1024
            ],
            [
                    'id'  : "io.reactivex:rxandroid:1.0.1",
                    'name': "rxandroid-1.0.1.aar",
                    'path': "Z:\\.gradle\\caches\\modules-2\\files-2.1\\io.reactivex\\rxandroid\\1.0.1\\563323d4f90cdb87d067fcf02b06c5d16bb1a258\\rxandroid-1.0.1.aar",
                    'size': 2000 * 1024
            ]
    ]

    def dictionary
    def fileCollection

    Set<File> prepareFiles() {
        Set<File> files = new HashSet<>()
        FILE_INFO.each {
            File f = mock(File)
            f.size().returns(it.size).stub()
            f.getPath().returns(it.path.replace('\\', File.separator)).stub()
            f.name.returns(it.name).stub()

            files.add(f)
        }
        files
    }

    @Override
    @Before
    void setUp() {
        super.setUp()

        fileCollection = mockFileCollection()
    }

    FileCollection mockFileCollection() {
        def fileCollection = mock(FileCollection)
        fileCollection.getFiles().returns(prepareFiles()).stub()
        return fileCollection
    }

    void prePlay() {
        dictionary = new DependencyDictionary(fileCollection)
    }

    @Test
    void testFindDependencyInfo() {
        play {
            prePlay()

            assert dictionary != null
            FILE_INFO.eachWithIndex { depend, index ->
                def info = dictionary.findDependencyInfo(depend.id)
                assert info.type.equals('aar')
                assert info.size == depend.size
                assert info.id.equals(depend.id)
            }
        }
    }


}
