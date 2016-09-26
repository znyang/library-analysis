package com.zen.plugin.lib.analysis.util

import org.junit.Test;

/**
 * @author zen
 * @version 2016/9/11
 */
class ResourceUtilsTest {

    private static final SEP = File.separator

    @Test
    public void testCopyResources() {
        def target = ".${SEP}build${SEP}test${SEP}"
        def path = new File(target).absolutePath
        println path

        ResourceUtils.copyResources(path)

        ResourceUtils.RESOURCE_FILES.each {
            def file = new File(target, it)
            assert file.exists()
//            if (file.isFile()) {
//                assert !file.text.equals("null")
//            }
        }
    }

    @Test
    public void testGetTemplate() {
        //assert !ResourceUtils.getTemplateFileContent("Tree.html").isEmpty()
    }
}
