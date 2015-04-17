package com.funnelback.publicui.test.search.model.curator.action;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.curator.Curator;
import com.funnelback.publicui.search.model.curator.action.TransformQuery;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class TransformQueryTests {

    @Test
    public void testSetQuery() {
        SearchTransaction searchTransaction = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        searchTransaction.getQuestion().setQuery("fooo");
        searchTransaction.getQuestion().getMetaParameters().add("a:foo");
        searchTransaction.getResponse().setCurator(new Curator());

        TransformQuery tq = new TransformQuery("f(o+)", "m$1", true);
        
        SearchTransaction st = ActionTestUtils.runAllPhases(tq, searchTransaction);
        
        Assert.assertEquals("Expected the query to be transformed", "mooo", st.getQuestion().getQuery());
        Assert.assertEquals("Expected the meta query to be transformed", "a:moo", st.getQuestion().getMetaParameters().get(0));
    }
}
