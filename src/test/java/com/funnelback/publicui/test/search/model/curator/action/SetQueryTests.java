package com.funnelback.publicui.test.search.model.curator.action;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.curator.Curator;
import com.funnelback.publicui.search.model.curator.action.SetQuery;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class SetQueryTests {

    @Test
    public void testSetQuery() {
        SearchTransaction searchTransaction = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        searchTransaction.getQuestion().getMetaParameters().add("foo");
        searchTransaction.getResponse().setCurator(new Curator());

        SetQuery sq = new SetQuery("set", false);
        
        SearchTransaction st = ActionTestUtils.runAllPhases(sq, searchTransaction);
        
        Assert.assertEquals("Expected the query to be set", "set", st.getQuestion().getQuery());
        Assert.assertFalse("Expected meta queries to be preserved", st.getQuestion().getMetaParameters().isEmpty());
    }

    @Test
    public void testClearingOfMetaQueries() {
        SearchTransaction searchTransaction = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        searchTransaction.getQuestion().getMetaParameters().add("foo");
        searchTransaction.getResponse().setCurator(new Curator());
        
        SetQuery sq = new SetQuery("set", true);
        
        SearchTransaction st = ActionTestUtils.runAllPhases(sq, searchTransaction);
        
        Assert.assertTrue("Expected meta queries to be emptied", st.getQuestion().getMetaParameters().isEmpty());
        Assert.assertEquals("Expected the query to be set", "set", st.getQuestion().getQuery());
    }
}
