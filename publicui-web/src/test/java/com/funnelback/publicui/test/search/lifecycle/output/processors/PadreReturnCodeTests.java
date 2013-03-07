package com.funnelback.publicui.test.search.lifecycle.output.processors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.PadreReturnCode;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.test.mock.MockLogService;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class PadreReturnCodeTests {

    private PadreReturnCode processor;
    
    private MockLogService logService;
    
    private SearchTransaction st;
    
    @Autowired
    private I18n i18n;
    
    @Before
    public void before() {
        logService = new MockLogService();
        processor = new PadreReturnCode();
        processor.setLogService(logService);
        processor.setI18n(i18n);
        
        st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        st.getQuestion().setCollection(new Collection("dummy", null));
    }
    
    @Test
    public void testMissingData() throws OutputProcessorException {
        // No transaction
        processor.processOutput(null);
        Assert.assertEquals(0, logService.getPublicUiWarnings().size());

        // No question nor response
        processor.processOutput(new SearchTransaction(null, null));
        Assert.assertEquals(0, logService.getPublicUiWarnings().size());
        
        // No question
        processor.processOutput(new SearchTransaction(new SearchQuestion(), null));
        Assert.assertEquals(0, logService.getPublicUiWarnings().size());

        // No return code
        SearchResponse response = new SearchResponse();
        processor.processOutput(new SearchTransaction(null, response));
        Assert.assertEquals(0, logService.getPublicUiWarnings().size());
    }
    
    @Test
    public void testCode0() throws OutputProcessorException {
        st.getResponse().setReturnCode(0);
        processor.processOutput(st);
        Assert.assertEquals(0, logService.getPublicUiWarnings().size());
    }
    
    @Test
    public void testCode199() throws OutputProcessorException {
        st.getResponse().setReturnCode(199);
        processor.processOutput(st);
        Assert.assertEquals(1, logService.getPublicUiWarnings().size());
        Assert.assertTrue(logService.getPublicUiWarnings().get(0).getMessage().contains("outputprocessor.padrereturncode.log.failed"));
        Assert.assertEquals("dummy", logService.getPublicUiWarnings().get(0).getCollection().getId());
    }

}
