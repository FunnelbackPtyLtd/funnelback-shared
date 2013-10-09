package com.funnelback.publicui.test.search.lifecycle.input.processors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.UserIdInQueryLogs;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.session.SearchSession;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;

public class UserIdInQueryLogsTest {

    private UserIdInQueryLogs processor;
    
    @Before
    public void before() {
        processor = new UserIdInQueryLogs();
    }
    
    
    @Test
    public void testMissingData() throws InputProcessorException {
        // No transaction
        processor.processInput(null);
        
        // No question
        processor.processInput(new SearchTransaction(null, null));
        
        // No collection
        processor.processInput(new SearchTransaction(new SearchQuestion(), null));
        
        // No sesssion
        SearchQuestion sq = new SearchQuestion();
        sq.setCollection(new Collection("dummy", null));
        processor.processInput(new SearchTransaction(sq, null));
        
        // No user
        SearchTransaction st = new SearchTransaction(sq, null);
        st.setSession(new SearchSession(null));
        processor.processInput(new SearchTransaction(sq, null));
    }
    
    @Test
    public void test() throws InputProcessorException {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        st.setSession(new SearchSession(new SearchUser("user-id")));
        
        processor.processInput(st);
        
        Assert.assertTrue(st.getQuestion().getDynamicQueryProcessorOptions().contains("-username=user-id"));
    }
    
    @Test
    public void testUserNoId() throws InputProcessorException {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        st.setSession(new SearchSession(new SearchUser(null)));
        
        processor.processInput(st);
        
        Assert.assertEquals(0, st.getQuestion().getDynamicQueryProcessorOptions().size());
    }

}
