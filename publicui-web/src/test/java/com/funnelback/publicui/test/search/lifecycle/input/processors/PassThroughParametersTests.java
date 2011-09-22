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
		
		st.getQuestion().getInputParameterMap().put(RequestParameters.QUERY, "query");
		st.getQuestion().getInputParameterMap().put(RequestParameters.COLLECTION, "collection");
		st.getQuestion().getInputParameterMap().put("param1", "value1");
		st.getQuestion().getInputParameterMap().put("param2", "value2a,value2b");
		st.getQuestion().getInputParameterMap().put(RequestParameters.ContextualNavigation.CN_CLICKED, "abc");
		st.getQuestion().getInputParameterMap().put(RequestParameters.ContextualNavigation.CN_PREV_PREFIX+"0", "def");
		
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
		
		
		Assert.assertEquals("value1", st.getQuestion().getAdditionalParameters().get("param1"));
		Assert.assertEquals("value2a,value2b", st.getQuestion().getAdditionalParameters().get("param2"));

	}

}
