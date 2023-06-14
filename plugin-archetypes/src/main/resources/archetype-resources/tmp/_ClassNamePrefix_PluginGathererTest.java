package ${package};

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.plugin.gatherer.mock.MockPluginGatherContext;
import com.funnelback.plugin.gatherer.mock.MockPluginStore;
import com.funnelback.plugin.gatherer.mock.MockVirusScanner;

public class _ClassNamePrefix_PluginGathererTest {

    @Test 
    public void testCustomGatherPlugin() throws Exception {
        MockPluginGatherContext mockContext = new MockPluginGatherContext();
        MockPluginStore mockStore = new MockPluginStore();
        MockVirusScanner mockVirusScanner = new MockVirusScanner();

        _ClassNamePrefix_PluginGatherer underTest = new _ClassNamePrefix_PluginGatherer();
        // As the plugin gatherer is likely to interact with an external system you may need
        // to mock those interactions out. Until that is done you can still use this test to
        // try out your gatherer locally.
        underTest.gather(mockContext, mockStore, mockVirusScanner);
        
        Assert.assertTrue("Check how many documents were stored.", mockStore.getStored().size() >= 0);
    }
    
}
