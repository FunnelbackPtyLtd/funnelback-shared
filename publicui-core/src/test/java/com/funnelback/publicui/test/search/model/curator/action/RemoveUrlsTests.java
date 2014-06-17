package com.funnelback.publicui.test.search.model.curator.action;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.curator.action.RemoveUrls;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

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
        ActionTestUtils.runAllPhases(ru2, st, null);
        
        Assert.assertEquals("Expected 'a b c d' as the list of URLs to remove",  "a b c d", st.getQuestion().getAdditionalParameters().get(RequestParameters.REMOVE_URLS)[0]);
    }

//    Needs to move to a CuratorYamlConfigResourceTest
//    @Test
//    public void testSerializeRemoveUrls() {
//        RemoveUrls ru = new RemoveUrls(Arrays.asList(new String[]{"uniqueurl"}));
//
//        String yaml = CuratorYamlConfigResource.getYamlObject().dumpAsMap(ru);
//        Assert.assertTrue("", yaml.contains("uniqueurl"));
//    }
}
