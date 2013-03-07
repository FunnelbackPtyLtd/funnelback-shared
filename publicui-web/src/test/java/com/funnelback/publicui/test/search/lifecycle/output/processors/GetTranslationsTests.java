package com.funnelback.publicui.test.search.lifecycle.output.processors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.GetTranslations;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.test.mock.MockConfigRepository;

public class GetTranslationsTests {

    private GetTranslations processor;
    private MockConfigRepository configRepository;
    private SearchTransaction st;
    
    @Before
    public void before() {
        configRepository = new MockConfigRepository();
        configRepository.getTranslations().put("search", "Search");
        
        processor = new GetTranslations();
        processor.setConfigRepository(configRepository);
        
        Collection c = new Collection("dummy",
                new NoOptionsConfig("dummy")
                    .setValue(Keys.ModernUI.I18N, "true"));
        SearchQuestion sq = new SearchQuestion();
        sq.setCollection(c);
        st = new SearchTransaction(sq, new SearchResponse());
    }
    
    @Test
    public void testMissingData() throws OutputProcessorException {
        // No transaction
        processor.processOutput(null);

        // No question and response
        processor.processOutput(new SearchTransaction(null, null));
        
        // No response
        processor.processOutput(new SearchTransaction(new SearchQuestion(), null));
        
        // No question
        processor.processOutput(new SearchTransaction(null, new SearchResponse()));
        
        // No locale
        SearchQuestion q = new SearchQuestion();
        q.setLocale(null);
        processor.processOutput(new SearchTransaction(q, null));
    }

    @Test
    public void testEnabled() throws OutputProcessorException {
        Assert.assertTrue(st.getResponse().getTranslations().isEmpty());
        processor.processOutput(st);
        Assert.assertFalse(st.getResponse().getTranslations().isEmpty());
        Assert.assertEquals("Search", st.getResponse().getTranslations().get("search"));
    }

    @Test
    public void testDisabled() throws OutputProcessorException {
        st.getQuestion().getCollection().getConfiguration().setValue(Keys.ModernUI.I18N, "false");        
        Assert.assertTrue(st.getResponse().getTranslations().isEmpty());
        processor.processOutput(st);
        Assert.assertTrue(st.getResponse().getTranslations().isEmpty());
        
    }

}
