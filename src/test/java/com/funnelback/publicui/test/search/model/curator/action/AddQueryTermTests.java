package com.funnelback.publicui.test.search.model.curator.action;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.curator.action.AddQueryTerm;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class AddQueryTermTests {

    @Test
    public void testAddQueryTerm() {
        AddQueryTerm aqt = new AddQueryTerm("added");
        
        SearchTransaction st = ActionTestUtils.runAllPhases(aqt);
        
        Assert.assertTrue("Expected message to be added to the response", st.getQuestion().getInputParameterMap().get(RequestParameters.S).equals("added"));
    }
}
