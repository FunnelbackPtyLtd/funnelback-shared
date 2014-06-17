package com.funnelback.publicui.test.search.model.curator.action;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.curator.action.DisplayProperties;
import com.funnelback.publicui.search.model.curator.data.Properties;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class DisplayPropertiesTests {

    @Test
    public void testDisplayProperties() {
        Properties properties = new Properties();
        DisplayProperties dp = new DisplayProperties(properties);
        
        SearchTransaction st = ActionTestUtils.runAllPhases(dp);
        
        Assert.assertTrue("Expected properties to be added to the response", st.getResponse().getCurator().getExhibits().contains(properties));
        Assert.assertEquals("Expected only one exhibit in the response", 1, st.getResponse().getCurator().getExhibits().size());
    }

//  Needs to move to a CuratorYamlConfigResourceTest
//    @Test
//    public void testSerializeDisplayProperties() {
//        Properties properties = new Properties();
//        properties.getProperties().put("uniquekey", "uniquevalue");
//        DisplayProperties dp = new DisplayProperties(properties);
//
//        String yaml = CuratorYamlConfigResource.getYamlObject().dumpAsMap(dp);
//        Assert.assertTrue("", yaml.contains("uniquekey"));
//        Assert.assertTrue("", yaml.contains("uniquevalue"));
//    }
}
