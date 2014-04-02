package com.funnelback.publicui.test.curator.action;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.curator.action.DisplayMessage;
import com.funnelback.publicui.search.model.curator.data.Message;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.resource.impl.CuratorConfigResource;

public class DisplayMessageTests {

    @Test
    public void testDisplayMessage() {
        Message message = new Message("html", null, "category");
        DisplayMessage dm = new DisplayMessage(message);
        
        SearchTransaction st = ActionTestUtils.runAllPhases(dm);
        
        Assert.assertTrue("Expected message to be added to the response", st.getResponse().getCurator().getExhibits().contains(message));
        Assert.assertEquals("Expected only one exhibit in the response", 1, st.getResponse().getCurator().getExhibits().size());
    }

    @Test
    public void testSerializeDisplayMessage() {
        Message message = new Message("uniquehtml", null, "category");
        DisplayMessage dm = new DisplayMessage(message);

        String yaml = CuratorConfigResource.getYamlObject().dumpAsMap(dm);
        Assert.assertTrue("", yaml.contains("uniquehtml"));
    }
}
