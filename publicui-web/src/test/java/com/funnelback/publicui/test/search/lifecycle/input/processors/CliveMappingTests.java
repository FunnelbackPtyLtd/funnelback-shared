package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.EnvironmentVariableException;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.CliveMapping;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;

public class CliveMappingTests {

	private CliveMapping processor = new CliveMapping();
	private SearchTransaction st;
	
	@Before
	public void before() throws FileNotFoundException, EnvironmentVariableException {
		processor = new CliveMapping();
		Collection c = new Collection("dummy", new NoOptionsConfig("dummy"));
		SearchQuestion question = new SearchQuestion();
		question.setCollection(c);
		st = new SearchTransaction(question, null);
	}
	

	@Test
	public void testInvalidParameters() {
		try {
			processor.processInput(null);
			processor.processInput(new SearchTransaction(null, null));
			processor.processInput(new SearchTransaction(new SearchQuestion(), null));
			processor.processInput(new SearchTransaction(new SearchQuestion(), null));
			st.getQuestion().setClive(new String[0]);
			processor.processInput(st);
		} catch (Exception e) {
			Assert.fail();
		}
	}
	
	@Test
	public void testCliveParameterButNoMetaComponents() throws InputProcessorException {
		st.getQuestion().setClive(new String[] {"clive1", "clive2"});
		processor.processInput(st);
		
		Assert.assertNull(st.getQuestion().getAdditionalParameters().get(RequestParameters.CLIVE));
	}
	
	@Test
	public void testNoCliveParameterButMetaComponents() throws InputProcessorException {
		st.getQuestion().getCollection().setMetaComponents(new String[] {"value1", "value2"});
		processor.processInput(st);
		
		Assert.assertNull(st.getQuestion().getClive());
		Assert.assertNull(st.getQuestion().getAdditionalParameters().get(RequestParameters.CLIVE));
	}
	
	@Test
	public void test() throws InputProcessorException {
		st.getQuestion().getCollection().setMetaComponents(new String[] {"component1", "component2", "component3"});
		st.getQuestion().setClive(new String[] {"component1", "component2"});

		processor.processInput(st);
		Assert.assertEquals("0,1", st.getQuestion().getAdditionalParameters().get(RequestParameters.CLIVE));
		
		st.getQuestion().setClive(new String[] {"component3"});
		processor.processInput(st);
		Assert.assertEquals("2", st.getQuestion().getAdditionalParameters().get(RequestParameters.CLIVE));
	}
	
	@Test
	public void testInvalidCliveParameter2() throws InputProcessorException {
		st.getQuestion().getCollection().setMetaComponents(new String[] {"component1", "component2", "component3"});
		st.getQuestion().setClive(new String[] {"invalid", "2", "component1"});

		processor.processInput(st);
		Assert.assertEquals("0", st.getQuestion().getAdditionalParameters().get(RequestParameters.CLIVE));
		
	}
	
}
