package com.zen.plugin.lib.analysis;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

/**
 * @author zen
 * @version 2016/6/7
 */
public class FileUtilsTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testConvertFileSize() throws Exception {
        long size = 1024 * 1024;
        String str = FileUtils.convertFileSize(size);
        Assert.assertEquals(str, "1.000 MB");

        size = 1024;
        str = FileUtils.convertFileSize(size);
        Assert.assertEquals(str, "1.000 KB");

        size *= 1024 * 1024 * 3;
        str = FileUtils.convertFileSize(size);
        Assert.assertEquals(str, "3.000 GB");
    }

    @Test
    public void testFileType() throws Exception {
        String fileName = "123.aar";
        Assert.assertEquals("aar", FileUtils.getFileType(fileName));

        fileName = "123";
        Assert.assertEquals(FileUtils.getFileType(fileName), fileName);
    }

    @Test
    public void testFileSize() throws Exception {
        File file = new File("src/test/resources", "./jarProject.jar");
        Assert.assertTrue(file.exists());
        Assert.assertEquals(file.length(), 1387);
        Assert.assertEquals(FileUtils.convertFileSize(file.length()), "1.354 KB");
    }
}
