package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.LocaleOverride;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;


public class LocaleOverrideTests {

    private LocaleOverride processor;
    private SearchTransaction st;
    
    @Before
    public void before() {
        processor = new LocaleOverride();
        st = new SearchTransaction(new SearchQuestion(), null);
        st.getQuestion().setLocale(null);
    }
    
    @Test
    public void testMissingData() throws InputProcessorException {
        // No transaction
        processor.processInput(null);
        
        // No question
        processor.processInput(new SearchTransaction(null, null));
    }

    
    @Test
    public void testNoOverride() throws InputProcessorException {
        processor.processInput(st);
        Assert.assertNull(st.getQuestion().getLocale());
    }
    
    @Test
    public void testLangUIOverride() throws InputProcessorException {
        st.getQuestion().getInputParameterMap().put("lang.ui", "fr_FR");
        processor.processInput(st);
        Assert.assertEquals(new Locale("fr", "FR"), st.getQuestion().getLocale());
    }

    @Test
    public void testLangOverride() throws InputProcessorException {
        st.getQuestion().getInputParameterMap().put("lang", "fr_FR");
        processor.processInput(st);
        Assert.assertEquals(new Locale("fr", "FR"), st.getQuestion().getLocale());
    }

    @Test
    public void testDoubleverride() throws InputProcessorException {
        st.getQuestion().getInputParameterMap().put("lang", "fr_FR");
        st.getQuestion().getInputParameterMap().put("lang.ui", "en_US");
        processor.processInput(st);
        Assert.assertEquals(new Locale("en", "US"), st.getQuestion().getLocale());
    }
    
    @Test
    public void testNoValue() throws InputProcessorException {
        st.getQuestion().getInputParameterMap().put("lang", null);
        processor.processInput(st);
        Assert.assertNull(st.getQuestion().getLocale());
        
        st.getQuestion().getInputParameterMap().put("lang", "");
        processor.processInput(st);
        Assert.assertNull(st.getQuestion().getLocale());

    }

    
    @Test
    public void testNoCountry() throws InputProcessorException {
        st.getQuestion().getInputParameterMap().put("lang.ui", "fr");
        processor.processInput(st);
        Assert.assertEquals(new Locale("fr"), st.getQuestion().getLocale());
        
        st.getQuestion().getInputParameterMap().put("lang.ui", "FR");
        processor.processInput(st);
        Assert.assertEquals(new Locale("fr"), st.getQuestion().getLocale());
    }
    
    @Test
    public void testInvalidLocale() throws InputProcessorException {
        st.getQuestion().getInputParameterMap().put("lang.ui", "invalid-locale");
        processor.processInput(st);
        // There's no such thing as an invalid locale apparently.
        Assert.assertEquals(new Locale("invalid-locale"), st.getQuestion().getLocale());
    }

}
