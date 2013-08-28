package com.funnelback.curator.action;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.curator.action.data.Properties;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.resource.impl.CuratorConifgResource;

public class DisplayPropertiesTests {

    @Test
    public void testDisplayProperties() {
        Properties properties = new Properties();
        DisplayProperties dp = new DisplayProperties(properties);
        
        SearchTransaction st = ActionTestUtils.runAllPhases(dp);
        
        Assert.assertTrue("Expected properties to be added to the response", st.getResponse().getCuratorModel().getExhibits().contains(properties));
        Assert.assertEquals("Expected only one exhibit in the response", 1, st.getResponse().getCuratorModel().getExhibits().size());
    }

    @Test
    public void testSerializeDisplayProperties() {
        Properties properties = new Properties();
        properties.getProperties().put("uniquekey", "uniquevalue");
        DisplayProperties dp = new DisplayProperties(properties);

        String yaml = CuratorConifgResource.getYamlObject().dumpAsMap(dp);
        Assert.assertTrue("", yaml.contains("uniquekey"));
        Assert.assertTrue("", yaml.contains("uniquevalue"));
    }
}
