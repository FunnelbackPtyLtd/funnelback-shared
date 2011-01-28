package com.funnelback.publicui.test.search.lifecycle.input.processors;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.PassThroughParameters;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class PassThroughParametersTests {

	
	@Test
	public void testMissingData() throws InputProcessorException {
		PassThroughParameters processor = new PassThroughParameters();
		
		// No transaction
		processor.process(null, null);
		
		// No question
		processor.process(new SearchTransaction(null, null), null);
		
		// No collection
		SearchQuestion question = new SearchQuestion();
		processor.process(new SearchTransaction(question, null), null);		
	}
	
	@Test
	public void test() {
		SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter(RequestParameters.QUERY, "query");
		request.setParameter(RequestParameters.COLLECTION, "collection");
		request.setParameter("param1", "value1");
		request.setParameter("param2", new String[]{"value2a", "value2b"});
		
		PassThroughParameters processor = new PassThroughParameters();
		processor.process(st, request);
		
		Assert.assertEquals(2, st.getQuestion().getPassThroughParameters().size());
		for (String ignored: PassThroughParameters.IGNORED) {
			Assert.assertFalse(st.getQuestion().getPassThroughParameters().containsKey(ignored));
		}
		
		Assert.assertEquals(1, st.getQuestion().getPassThroughParameters().get("param1").length);
		Assert.assertEquals("value1", st.getQuestion().getPassThroughParameters().get("param1")[0]);
		
		Assert.assertEquals(2, st.getQuestion().getPassThroughParameters().get("param2").length);
		Assert.assertEquals("value2a", st.getQuestion().getPassThroughParameters().get("param2")[0]);
		Assert.assertEquals("value2b", st.getQuestion().getPassThroughParameters().get("param2")[1]);

	}

}
