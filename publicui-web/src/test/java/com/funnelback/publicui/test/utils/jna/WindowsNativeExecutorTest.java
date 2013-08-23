package com.funnelback.publicui.test.utils.jna;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.OS;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.utils.ExecutionReturn;
import com.funnelback.publicui.utils.jna.WindowsNativeExecutor;
import com.funnelback.publicui.utils.jna.WindowsNativeExecutor.ExecutionException;

public class WindowsNativeExecutorTest {

    @BeforeClass
    public static void beforeClass() {
        Assume.assumeTrue(OS.isFamilyWindows());
    }
    
    @Test
    public void testNoEnvironment() throws ExecutionException {
        WindowsNativeExecutor executor = new WindowsNativeExecutor(new I18n(), 1000*30);
        
        ExecutionReturn er = executor.execute(Arrays.asList(new String[]{"net.exe"}), null);
        
        Assert.assertEquals(1, er.getReturnCode());
        Assert.assertTrue(er.getOutput().contains("The syntax of this command is"));
    }

    @Test
    public void testEnvironmentAndUnicode() throws ExecutionException {
        WindowsNativeExecutor executor = new WindowsNativeExecutor(new I18n(), 1000*30);
        
        Map<String, String> env = new HashMap<String, String>();
        env.put("TEST_VAR", "test value");
        ExecutionReturn er = executor.execute(Arrays.asList(new String[]{"cmd.exe","/c","echo","%TEST_VAR%"}), env);
        
        Assert.assertEquals(0, er.getReturnCode());
        Assert.assertEquals("test value", er.getOutput().trim());
    }

    
}
