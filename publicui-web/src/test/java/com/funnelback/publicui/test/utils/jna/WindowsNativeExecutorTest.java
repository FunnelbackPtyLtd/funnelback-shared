package com.funnelback.publicui.test.utils.jna;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.OS;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.utils.ExecutionReturn;
import com.funnelback.publicui.utils.jna.WindowsNativeExecutor;
import com.funnelback.publicui.utils.jna.WindowsNativeExecutor.ExecutionException;

public class WindowsNativeExecutorTest {

    private I18n i18n;
    
    @BeforeClass
    public static void beforeClass() {
        Assume.assumeTrue(OS.isFamilyWindows());
    }
    
    @Before
    public void before() {
        ResourceBundleMessageSource msg = new ResourceBundleMessageSource();
        msg.setUseCodeAsDefaultMessage(true);
        i18n = new I18n();
        i18n.setMessages(msg);
    }
    
    @Test
    public void testNoEnvironment() throws ExecutionException, IOException {
        WindowsNativeExecutor executor = new WindowsNativeExecutor(i18n, 1000*30);
        
        ExecutionReturn er = executor.execute(Arrays.asList(new String[]{"net.exe"}), null, 32, Integer.MAX_VALUE);
        
        Assert.assertEquals(1, er.getReturnCode());
        Assert.assertTrue(new String(IOUtils.toByteArray(er.getOutBytes()), StandardCharsets.UTF_8).contains("The syntax of this command is"));
    }

    @Test
    public void testEnvironment() throws ExecutionException, IOException {
        WindowsNativeExecutor executor = new WindowsNativeExecutor(i18n, 1000*30);
        
        Map<String, String> env = new HashMap<String, String>();
        env.put("TEST_VAR", "test value");
        ExecutionReturn er = executor.execute(Arrays.asList(new String[]{"src/test/resources/dummy-search_home/bin/getenv.exe", "TEST_VAR"}), env, 32, Integer.MAX_VALUE);
        
        String out = new String(IOUtils.toByteArray(er.getOutBytes()), StandardCharsets.UTF_8).trim();
        Assert.assertEquals(out, 0, er.getReturnCode());
        Assert.assertEquals("test value", out);
    }

    
}
