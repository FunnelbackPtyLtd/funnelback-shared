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
		processor.process(null, null);
		
		// No question
		processor.process(new SearchTransaction(null, null), null);
		
		// No collection
		SearchQuestion question = new SearchQuestion();
		processor.process(new SearchTransaction(question, null), null);		
	}
	
	@Test
	public void testEmptyRequest() {
		PassThroughEnvironmentVariables processor = new PassThroughEnvironmentVariables();
		SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRemoteAddr(null);
		request.setRequestURI(null);
		
		processor.process(st, request);
		
		Assert.assertEquals(0, st.getQuestion().getEnvironmentVariables().size());
	}
	
	@Test
	public void test() {
		PassThroughEnvironmentVariables processor = new PassThroughEnvironmentVariables();
		SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRemoteAddr("1.2.3.4");
		request.setRequestURI("/uri");
		request.setAuthType("authtype");
		request.addHeader("host", "host.com");
		request.setRemoteUser("batman");
		
		processor.process(st, request);
		
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
