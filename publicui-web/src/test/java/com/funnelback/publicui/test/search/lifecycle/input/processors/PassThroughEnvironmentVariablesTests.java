package com.funnelback.publicui.test.search.lifecycle.input.processors;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.PassThroughEnvironmentVariables;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class PassThroughEnvironmentVariablesTests {

	@Test
	public void testMissingData() throws InputProcessorException {
		PassThroughEnvironmentVariables processor = new PassThroughEnvironmentVariables();
		
		// No transaction
		processor.processInput(null);
		
		// No question
		processor.processInput(new SearchTransaction(null, null));
		
		// No collection
		SearchQuestion question = new SearchQuestion();
		processor.processInput(new SearchTransaction(question, null));		
	}
	
	@Test
	public void testEmptyParams() {
		PassThroughEnvironmentVariables processor = new PassThroughEnvironmentVariables();
		SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
		
		processor.processInput(st);
		
		Assert.assertEquals(0, st.getQuestion().getEnvironmentVariables().size());
	}
	
	@Test
	public void test() {
		PassThroughEnvironmentVariables processor = new PassThroughEnvironmentVariables();
		SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
		
		st.getQuestion().getInputParameterMap().put("REMOTE_ADDR", new String[] {"1.2.3.4"});
		st.getQuestion().getInputParameterMap().put("REQUEST_URI", new String[] {"/uri"});
		st.getQuestion().getInputParameterMap().put("AUTH_TYPE", new String[] {"authtype"});
		st.getQuestion().getInputParameterMap().put("HTTP_HOST", new String[] {"host.com"});
		st.getQuestion().getInputParameterMap().put("REMOTE_USER", new String[] {"batman"});
		
		processor.processInput(st);
		
		Assert.assertEquals(5, st.getQuestion().getEnvironmentVariables().size());
		Assert.assertEquals(
				"1.2.3.4",
				st.getQuestion().getEnvironmentVariables().get(PassThroughEnvironmentVariables.Keys.REMOTE_ADDR.toString()));
		Assert.assertEquals(
				"/uri",
				st.getQuestion().getEnvironmentVariables().get(PassThroughEnvironmentVariables.Keys.REQUEST_URI.toString()));
		Assert.assertEquals(
				"authtype",
				st.getQuestion().getEnvironmentVariables().get(PassThroughEnvironmentVariables.Keys.AUTH_TYPE.toString()));
		Assert.assertEquals(
				"host.com",
				st.getQuestion().getEnvironmentVariables().get(PassThroughEnvironmentVariables.Keys.HTTP_HOST.toString()));
		Assert.assertEquals(
				"batman",
				st.getQuestion().getEnvironmentVariables().get(PassThroughEnvironmentVariables.Keys.REMOTE_USER.toString()));
	}
}
