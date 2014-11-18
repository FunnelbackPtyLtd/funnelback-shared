package com.funnelback.publicui.form.converter.test;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.funnelback.publicui.form.converter.Main;

public class MainTests {

    /**
     * Tests converting simple.form.dist
     * @throws IOException
     */
    @Test
    public void test() throws IOException {
        File in = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "simple.form.dist");
        File expectedFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "simple.ftl.dist");
        File out = File.createTempFile(MainTests.class.getName(), ".tmp");
        out.deleteOnExit();
        
        String[] args = {
                "-v",
                "-i ", in.getAbsolutePath(),
                "-o ", out.getAbsolutePath()
        };
        
        Main.main(args);
        
        Assert.assertEquals(
                FileUtils.readFileToString(expectedFile).replaceAll("\r?\n", "\n"),
                FileUtils.readFileToString(out).replaceAll("\r?\n", "\n")
                );
    }

    /**
     * Tests for tags that are not present in simple.form.dist
     */
    @Test
    public void testNotInDistForm() throws IOException {
        File in = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "tests.form");
        File expectedFile = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "tests.ftl");
        File out = File.createTempFile(MainTests.class.getName(), "tmp");
        out.deleteOnExit();
        
        String[] args = {
                "-i ", in.getAbsolutePath(),
                "-o ", out.getAbsolutePath()
        };
        
        Main.main(args);
        
        Assert.assertEquals(
                FileUtils.readFileToString(expectedFile).replaceAll("\r?\n", "\n"),
                FileUtils.readFileToString(out).replaceAll("\r?\n", "\n")
                );
    }
    
}
