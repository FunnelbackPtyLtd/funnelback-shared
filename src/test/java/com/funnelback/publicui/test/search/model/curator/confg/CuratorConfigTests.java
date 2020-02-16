package com.funnelback.publicui.test.search.model.curator.confg;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.curator.config.CuratorConfig;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;

public class CuratorConfigTests {

    @Test
    public void testIsCuratorActiveByDefault() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        
        Assert.assertTrue("Expected curator to be enabled by default", CuratorConfig.isCuratorActive(st));
    }

    @Test
    public void testIsCuratorActiveDisabling() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        
        st.getQuestion().getInputParameterMap().put(RequestParameters.CURATOR, "false");

        Assert.assertFalse("Expected curator to be disabled when curator=false", CuratorConfig.isCuratorActive(st));
    }

    @Test
    public void testIsCuratorActiveEnabling() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        
        st.getQuestion().getInputParameterMap().put(RequestParameters.CURATOR, "true");

        Assert.assertTrue("Expected curator to be disabled when curator=true", CuratorConfig.isCuratorActive(st));
    }
}
