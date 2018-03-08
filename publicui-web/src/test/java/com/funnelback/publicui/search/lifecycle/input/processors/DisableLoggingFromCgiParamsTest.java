package com.funnelback.publicui.search.lifecycle.input.processors;

import static org.mockito.Mockito.mock;

import org.junit.Assert;
import org.junit.Test;

import java.security.Principal;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
public class DisableLoggingFromCgiParamsTest {

    @Test
    public void testDisableLogging () {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        Principal principal = mock(Principal.class);
        st.getQuestion().setPrincipal(principal);
        for(String logValues : new String[]{"off", "False", "0"}) {
            st.getQuestion().getInputParameterMap().put("log", logValues);
            st.getQuestion().setLogQuery(true);
            Assert.assertTrue(st.getQuestion().isLogQuery());
            new DisableLoggingFromCgiParams().processInput(st);
            Assert.assertFalse(st.getQuestion().isLogQuery());
        }
    }
    
    @Test
    public void testNonAuthenticatedRequestsCanNotDisableLogging() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        st.getQuestion().setPrincipal(null);
        st.getQuestion().getInputParameterMap().put("log", "off");
        st.getQuestion().setLogQuery(true);
        Assert.assertTrue(st.getQuestion().isLogQuery());
        new DisableLoggingFromCgiParams().processInput(st);
        Assert.assertTrue(st.getQuestion().isLogQuery());
    }
    
    @Test
    public void testOtherValuesIgnored() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        Principal principal = mock(Principal.class);
        st.getQuestion().setPrincipal(principal);
        st.getQuestion().getInputParameterMap().put("log", "what");
        st.getQuestion().setLogQuery(true);
        Assert.assertTrue(st.getQuestion().isLogQuery());
        new DisableLoggingFromCgiParams().processInput(st);
        Assert.assertTrue(st.getQuestion().isLogQuery());
    }
}
