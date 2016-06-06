package com.zen.plugin.lib.analysis;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author zen
 * @version 2016/6/7
 */
public class FileUtilsTest {

    @Test
    public void testConvertFileSize() throws Exception {
        long size = 1024 * 1024;
        String str = FileUtils.convertFileSize(size);
        Assert.assertEquals(str, "1.000 MB");

        size = 1024;
        str = FileUtils.convertFileSize(size);
        Assert.assertEquals(str, "1.000 KB");
    }

    @Test
    public void testFileType() throws Exception {
        String fileName = "123.aar";
        Assert.assertEquals("aar", FileUtils.getFileType(fileName));
    }
}
