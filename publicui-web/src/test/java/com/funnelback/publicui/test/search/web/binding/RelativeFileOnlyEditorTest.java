package com.funnelback.publicui.test.search.web.binding;

import java.io.File;

import org.apache.commons.exec.OS;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.web.binding.RelativeFileOnlyEditor;

public class RelativeFileOnlyEditorTest {

    private RelativeFileOnlyEditor editor;
    
    @Before
    public void before() {
        editor = new RelativeFileOnlyEditor();
    }
    
    @Test
    public void testRelativeFile() {
        editor.setAsText("folder/file.txt");
        Assert.assertEquals(
            new File("folder/file.txt"),
            editor.getValue());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testAbsoluteFile() {
        if (OS.isFamilyWindows()) {
            editor.setAsText("C:\\Windows\\System32\\cmd.exe");
        } else {
            editor.setAsText("/etc/passwd");
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testParentFile() {
        editor.setAsText("../file.txt");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSneakyParentFile() {
        editor.setAsText("folder/sub-folder/../../../file.txt");
    }

    
}
