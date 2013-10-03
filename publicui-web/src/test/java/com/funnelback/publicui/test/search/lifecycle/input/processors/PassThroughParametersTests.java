package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.PassThroughParameters;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;

public class PassThroughParametersTests {
    
    @Test
    public void testMissingData() throws InputProcessorException {
        PassThroughParameters processor = new PassThroughParameters();
        
        // No transaction
        processor.processInput(null);
        
        // No question
        processor.processInput(new SearchTransaction(null, null));
        
        // No collection
        SearchQuestion question = new SearchQuestion();
        processor.processInput(new SearchTransaction(question, null));        
    }
    
    @Test
    public void test() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        
        st.getQuestion().getRawInputParameters().put(RequestParameters.QUERY, new String[] {"query"});
        st.getQuestion().getRawInputParameters().put(RequestParameters.S, new String[] {"system query"});
        st.getQuestion().getRawInputParameters().put(RequestParameters.COLLECTION, new String[] {"collection"});
        st.getQuestion().getRawInputParameters().put("param1", new String[] {"value1"});
        st.getQuestion().getRawInputParameters().put("param2", new String[] {"value2a,value2b"});
        st.getQuestion().getRawInputParameters().put(RequestParameters.ContextualNavigation.CN_CLICKED, new String[] {"abc"});
        st.getQuestion().getRawInputParameters().put(RequestParameters.ContextualNavigation.CN_PREV_PREFIX+"0", new String[] {"def"});
        
        PassThroughParameters processor = new PassThroughParameters();
        processor.processInput(st);
        
        // Only our 2 custom params should be there
        Assert.assertEquals(2, st.getQuestion().getAdditionalParameters().size());
        
        // Ignored params shouldn't be there
        for (String ignored: PassThroughParameters.IGNORED_NAMES) {
            Assert.assertFalse(st.getQuestion().getAdditionalParameters().containsKey(ignored));
        }
        for (Pattern ignored: PassThroughParameters.IGNORED_PATTERNS) {
            for(String key: st.getQuestion().getAdditionalParameters().keySet()) {
                Assert.assertFalse(ignored.matcher(key).matches());
            }
        }
        
        
        Assert.assertArrayEquals(new String[] {"value1"}, st.getQuestion().getAdditionalParameters().get("param1"));
        Assert.assertArrayEquals(new String[] {"value2a,value2b"}, st.getQuestion().getAdditionalParameters().get("param2"));

    }

}
