package com.funnelback.publicui.test.search.model.curator.action;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.curator.Curator;
import com.funnelback.publicui.search.model.curator.action.ReplaceQueryTerm;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class ReplaceQueryTermTests {

    @Test
    public void testReplaceQueryTerm() {
        SearchTransaction searchTransaction = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        searchTransaction.getQuestion().getMetaParameters().add("a:\"foo gar\"");
        searchTransaction.getQuestion().setQuery("goo foo gar");
        searchTransaction.getResponse().setCurator(new Curator());

        ReplaceQueryTerm rqt = new ReplaceQueryTerm("foo", "bar");
        
        SearchTransaction st = ActionTestUtils.runAllPhases(rqt, searchTransaction);
        
        Assert.assertEquals("Expected the query to have foo replaced with bar", "goo bar gar", st.getQuestion().getQuery());
        Assert.assertEquals("Expected the meta query to have foo replaced with bar", "a:\"bar gar\"", st.getQuestion().getMetaParameters().get(0));
    }
}
