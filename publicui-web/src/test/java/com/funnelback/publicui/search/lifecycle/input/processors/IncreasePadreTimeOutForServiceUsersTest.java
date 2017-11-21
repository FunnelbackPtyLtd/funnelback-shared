package com.funnelback.publicui.search.lifecycle.input.processors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.system.Security;
import com.funnelback.common.system.Security.System;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreForkingOptionsHelper;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class IncreasePadreTimeOutForServiceUsersTest {
    
    @Test
    public void testNulls() {
        IncreasePadreTimeOutForServiceUsers processor = new IncreasePadreTimeOutForServiceUsers();
        processor.processInput(null);
        processor.processInput(new SearchTransaction());
        processor.processInput(new SearchTransaction(new SearchQuestion(), null));
        
        // Test when the principal exist but the user is still null, we should not set the timeout.
        SearchTransaction st = searchTransactionWithUser(null);
        processor.processInput(st);
        Assert.assertFalse(st.getQuestion().getPadreTimeout().isPresent());
    }
    
    @Test
    public void testNonSystemUser() {
        SearchTransaction st = searchTransactionWithUser("admin");
        processorWithDefaultTimeout(10).processInput(st);
        Assert.assertFalse("Timeout should not be increaed for non system users",
            st.getQuestion().getPadreTimeout().isPresent());
    }
    
    @Test
    public void testSystemUser() {
        SearchTransaction st = searchTransactionWithUser(Security.getServiceAccountName(System.COLLECTION_UPDATE));
        processorWithDefaultTimeout(10).processInput(st);
        Assert.assertTrue("Timeout should be set for system users",
            st.getQuestion().getPadreTimeout().isPresent());
    }
    
    @Test
    public void timeoutShouldNotBeMadeSmaller() {
        SearchTransaction st = searchTransactionWithUser(Security.getServiceAccountName(System.COLLECTION_UPDATE));
        processorWithDefaultTimeout(1000000000).processInput(st);
        Assert.assertFalse("Timeout should only be increased not made smaller",
            st.getQuestion().getPadreTimeout().isPresent());
    }
    
    
    private SearchTransaction searchTransactionWithUser(String user) {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        st.getQuestion().setPrincipal(mockPrinciple(user));
        return st;
    }
    
    
    private Principal mockPrinciple(String user) {
        return new Principal() {
            
            @Override
            public String getName() {
                return user;
            }
        };
    }
    
    private IncreasePadreTimeOutForServiceUsers processorWithDefaultTimeout(long configSetPadreTimeout) {
        PadreForkingOptionsHelper optionsHelper = mock(PadreForkingOptionsHelper.class);
        when(optionsHelper.getPadreForkingTimeout()).thenReturn(configSetPadreTimeout);
        
        IncreasePadreTimeOutForServiceUsers processor = new IncreasePadreTimeOutForServiceUsers() {
            @Override
            protected PadreForkingOptionsHelper getPadreForkingOptionsHelper(SearchTransaction st) {
                return optionsHelper;
            }
        };
        
        
        
        return processor;
    }
    
    
}

