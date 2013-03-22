package com.funnelback.publicui.test.utils.jna;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.exec.OS;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import com.funnelback.publicui.utils.jna.WindowsFileInputStream;

public class WindowsFileInputStreamTest {

    @BeforeClass
    public static void beforeClass() {
        Assume.assumeTrue(OS.isFamilyWindows());
    }
    
    @Test
    public void test() throws IOException {
        File f = new File("src/test/resources/utils/file-input-stream/file.txt");
        
        WindowsFileInputStream wis = new WindowsFileInputStream(f.getAbsolutePath());
        FileInputStream is = new FileInputStream(f);
        
        byte[] winBytes = IOUtils.toByteArray(wis);
        byte[] javaBytes = IOUtils.toByteArray(is);
        
        Assert.assertTrue(winBytes.length > 0);
        Assert.assertArrayEquals(javaBytes, winBytes);
    }
    
    @Test
    public void testUnicode() throws IOException {
        File f = new File("src/test/resources/utils/file-input-stream/\u65E5\u672C.txt");
        
        WindowsFileInputStream wis = new WindowsFileInputStream(f.getAbsolutePath());
        FileInputStream is = new FileInputStream(f);
        
        byte[] winBytes = IOUtils.toByteArray(wis);
        byte[] javaBytes = IOUtils.toByteArray(is);
        
        Assert.assertTrue(winBytes.length > 0);
        Assert.assertArrayEquals(javaBytes, winBytes);
    }
    
}
