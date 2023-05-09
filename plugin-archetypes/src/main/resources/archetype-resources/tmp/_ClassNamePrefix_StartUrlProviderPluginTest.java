package ${package};

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.plugin.starturls.mock.MockStartUrlProviderContext;

import java.net.URL;
import java.util.List;

public class _ClassNamePrefix_StartUrlProviderPluginTest {

    @Test
    public void testStartUrlProviderPlugin(){
        MockStartUrlProviderContext mockContext = new MockStartUrlProviderContext();
        /*
            The code below explains how to access pluginutils via plugin class.
        */

        _ClassNamePrefix_StartUrlProviderPlugin underTest = new _ClassNamePrefix_StartUrlProviderPlugin();
        String testKey = underTest.pluginUtils.LIST_KEY.getKey();
        mockContext.setConfigSetting(testKey, "test value");
        // Call the class.
        List<URL> urlList = underTest.extraStartUrls(mockContext);
        // Assertions
        Assert.assertTrue(urlList.isEmpty());
    }
    
}
