package com.funnelback.curator.action;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.resource.impl.CuratorConifgResource;

public class PromoteUrlsTests {

    @Test
    public void testPromoteUrls() {
        PromoteUrls pu = new PromoteUrls(Arrays.asList(new String[]{"a", "b"}));
        
        SearchTransaction st = ActionTestUtils.runAllPhases(pu);
        
        Assert.assertEquals("Expected 'a b' as the list of URLs to promote",  "a b", st.getQuestion().getAdditionalParameters().get(RequestParameters.PROMOTE_URLS)[0]);
    }

    @Test
    public void testMultiplePromoteUrls() {
        PromoteUrls pu1 = new PromoteUrls(Arrays.asList(new String[]{"a", "b"}));
        PromoteUrls pu2 = new PromoteUrls(Arrays.asList(new String[]{"c", "d"}));
        
        SearchTransaction st = ActionTestUtils.runAllPhases(pu1);
        ActionTestUtils.runAllPhases(pu2, st);
        
        Assert.assertEquals("Expected 'a b c d' as the list of URLs to promote",  "a b c d", st.getQuestion().getAdditionalParameters().get(RequestParameters.PROMOTE_URLS)[0]);
    }

    @Test
    public void testSerializePromoteUrls() {
        PromoteUrls pu = new PromoteUrls(Arrays.asList(new String[]{"uniqueurl"}));

        String yaml = CuratorConifgResource.getYamlObject().dumpAsMap(pu);
        Assert.assertTrue("", yaml.contains("uniqueurl"));
    }
}
