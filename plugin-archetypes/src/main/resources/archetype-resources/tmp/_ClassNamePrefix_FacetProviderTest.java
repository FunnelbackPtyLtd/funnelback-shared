package ${package};

import org.junit.jupiter.api.Test;

import com.funnelback.plugin.index.mock.MockIndexConfigProviderContext;

public class _ClassNamePrefix_FacetProviderTest {

    @Test
    public void testFacetsPlugin() {
        MockIndexConfigProviderContext mockContext = new MockIndexConfigProviderContext();
        /*
            The code below explains how to access pluginutils via plugin class.
        */

        _ClassNamePrefix_FacetProvider underTest = new _ClassNamePrefix_FacetProvider();
        String testKey = underTest.pluginUtils.LIST_KEY.getKey();
        mockContext.setConfigSetting(testKey, "test value");
        // Call the class.
        new _ClassNamePrefix_FacetProvider().extraFacetedNavigation(mockContext);
    }
}