package com.zen.plugin.lib.analysis.ext

import org.junit.Test

/**
 * @author zen
 * @version 2016/9/11
 */
class LibraryAnalysisExtensionTest {

    static final def data = [
            [
                    'size' : 9 * 1024,
                    'style': 'tag-normal'
            ],
            [
                    'size' : 300 * 1024,
                    'style': 'tag-warning'
            ],
            [
                    'size' : 2048 * 1024,
                    'style': 'tag-danger'
            ]
    ]

    static final def IGNORE = [
            "com.android.support": [
                    ['target': 'com.android.support:support-v4:22.2.0', 'result': true],
                    ['target': 'com.squareup.retrofit2:retrofit:2.1.0', 'result': false]
            ]
    ]

    @Test
    public void testGetSIzeStyle() throws Exception {
        data.each {
            assert new LibraryAnalysisExtension().getSizeTag(it.size).equals(it.style)
        }
    }

    @Test
    public void testIsIgnore() throws Exception {
        def ext = new LibraryAnalysisExtension()
        IGNORE.each {
            ext.ignore = [it.key]
            it.value.each { data ->
                assert ext.isIgnore(data.target) == data.result
            }
        }
    }
}
