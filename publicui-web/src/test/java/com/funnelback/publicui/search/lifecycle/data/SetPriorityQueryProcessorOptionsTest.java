package com.funnelback.publicui.search.lifecycle.data;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class SetPriorityQueryProcessorOptionsTest {

    @Test
    public void test() throws Exception {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        st.getQuestion().getAdditionalParameters().put("key", new String[]{"old"});
        st.getQuestion().getInputParameterMap().put("key", "old");
        st.getQuestion().getDynamicQueryProcessorOptions().add("-key=b");
        st.getQuestion().getDynamicQueryProcessorOptions().add("-keys=b");
        
        st.getQuestion().getPriorityQueryProcessorOptions().addOption("key", "wanted");
        
        new SetPriorityQueryProcessorOptions().fetchData(st);
        
        Assert.assertFalse(st.getQuestion().getAdditionalParameters().containsKey("key"));
        Assert.assertFalse(st.getQuestion().getInputParameterMap().containsKey("key"));
        Assert.assertFalse(st.getQuestion().getDynamicQueryProcessorOptions().contains("-key=b"));
        
        Assert.assertTrue("Should have added the wanted key to the dynamic QPO",
            st.getQuestion().getDynamicQueryProcessorOptions().contains("-key=wanted"));
        Assert.assertTrue("Should not have removed options that where not overwritten.",
            st.getQuestion().getDynamicQueryProcessorOptions().contains("-keys=b"));
        
    }
}
