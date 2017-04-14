package com.zen.plugin.lib.analysis.render

import org.junit.Assert
import org.junit.Test

/**
 *
 * @author: zen
 * date: 2017/4/14 0014.
 */
class TextRendererTest {

    @Test
    void testTextAlign() {
        def content = "123"
        def target = "      123"
        def aligned = TextRenderer.textAlign(content, 9)
        Assert.assertEquals(aligned, target)
    }
}
