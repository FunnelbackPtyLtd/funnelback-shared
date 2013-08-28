package com.funnelback.curator.action;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.resource.impl.CuratorConifgResource;

public class RemoveUrlsTests {

    @Test
    public void testRemoveUrls() {
        RemoveUrls ru = new RemoveUrls(Arrays.asList(new String[]{"a", "b"}));
        
        SearchTransaction st = ActionTestUtils.runAllPhases(ru);
        
        Assert.assertEquals("Expected 'a b' as the list of URLs to remove",  "a b", st.getQuestion().getAdditionalParameters().get(RequestParameters.REMOVE_URLS)[0]);
    }

    @Test
    public void testMultipleRemoveUrls() {
        RemoveUrls ru1 = new RemoveUrls(Arrays.asList(new String[]{"a", "b"}));
        RemoveUrls ru2 = new RemoveUrls(Arrays.asList(new String[]{"c", "d"}));
        
        SearchTransaction st = ActionTestUtils.runAllPhases(ru1);
        ActionTestUtils.runAllPhases(ru2, st);
        
        Assert.assertEquals("Expected 'a b c d' as the list of URLs to remove",  "a b c d", st.getQuestion().getAdditionalParameters().get(RequestParameters.REMOVE_URLS)[0]);
    }

    @Test
    public void testSerializeRemoveUrls() {
        RemoveUrls ru = new RemoveUrls(Arrays.asList(new String[]{"uniqueurl"}));

        String yaml = CuratorConifgResource.getYamlObject().dumpAsMap(ru);
        Assert.assertTrue("", yaml.contains("uniqueurl"));
    }
}
