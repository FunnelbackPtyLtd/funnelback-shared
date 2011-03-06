package com.funnelback.publicui.test.search.lifecycle;

import groovy.lang.GroovyClassLoader;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.lifecycle.GenericHookScriptRunner;
import com.funnelback.publicui.search.lifecycle.GenericHookScriptRunner.Phase;
import com.funnelback.publicui.search.lifecycle.data.DataFetchException;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Collection.Hook;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class GenericHookScriptRunnerTests {

	private SearchTransaction st;
	
	@SuppressWarnings("unchecked")
	@Before
	public void before() {
		Collection c = new Collection("dummy", null);
		c.getHookScriptsClasses().put(Hook.pre_datafetch, new GroovyClassLoader().parseClass("transaction.question.query = \"New Query\""));
		c.getHookScriptsClasses().put(Hook.post_datafetch, new GroovyClassLoader().parseClass("transaction.question.cnClickedCluster = \"Clicked Cluster\""));
		
		SearchQuestion sq = new SearchQuestion();
		sq.setCollection(c);
		sq.setQuery("Test query");
		
		st = new SearchTransaction(sq, null);
	}
	
	@Test
	public void testMissingData() throws DataFetchException {
		GenericHookScriptRunner processor = new GenericHookScriptRunner(Hook.pre_datafetch, Phase.Data);

		// No transaction
		processor.fetchData(null);

		// No question / response
		processor.fetchData(new SearchTransaction(null, null));

		// No collection
		SearchResponse response = new SearchResponse();
		processor.fetchData(new SearchTransaction(new SearchQuestion(), null));

		// No hook scripts
		SearchQuestion q = new SearchQuestion();
		q.setCollection(new Collection("dummy", null));
		processor.fetchData(new SearchTransaction(q, response));
	}

	@Test
	public void testNoScriptForAPhase() throws Exception {
		GenericHookScriptRunner processor = new GenericHookScriptRunner(Hook.pre_datafetch, Phase.Data);
		st.getQuestion().getCollection().getHookScriptsClasses().remove(Hook.pre_datafetch);
		
		processor.process(st, null);
		processor.process(st);
		processor.fetchData(st);
		
		Assert.assertEquals("Test query", st.getQuestion().getQuery());
		Assert.assertNull(st.getQuestion().getCnClickedCluster());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void scriptThrowingException() throws Exception {
		GenericHookScriptRunner processor = new GenericHookScriptRunner(Hook.pre_datafetch, Phase.Data);
		st.getQuestion().getCollection().getHookScriptsClasses().put(Hook.pre_datafetch, new GroovyClassLoader().parseClass("throw new RuntimeException()"));
		
		processor.process(st, null);
		processor.process(st);
		processor.fetchData(st);
		
		Assert.assertEquals("Test query", st.getQuestion().getQuery());
		Assert.assertNull(st.getQuestion().getCnClickedCluster());		
	}
	
	@Test
	public void testRunInDataPhase() throws Exception {
		GenericHookScriptRunner processor = new GenericHookScriptRunner(Hook.pre_datafetch, Phase.Data);
		
		processor.process(st, null);
		processor.process(st);
		Assert.assertEquals("Test query", st.getQuestion().getQuery());
		Assert.assertNull(st.getQuestion().getCnClickedCluster());
		
		processor.fetchData(st);
		
		Assert.assertEquals("New Query", st.getQuestion().getQuery());
		Assert.assertNull(st.getQuestion().getCnClickedCluster());
	}
	
	@Test
	public void testRunInInputPhase() throws Exception {
		GenericHookScriptRunner processor = new GenericHookScriptRunner(Hook.post_datafetch, Phase.Input);
		
		processor.fetchData(st);
		processor.process(st);
		Assert.assertEquals("Test query", st.getQuestion().getQuery());
		Assert.assertNull(st.getQuestion().getCnClickedCluster());
		
		processor.process(st, null);
		
		Assert.assertEquals("Test query", st.getQuestion().getQuery());
		Assert.assertEquals("Clicked Cluster", st.getQuestion().getCnClickedCluster());		
	}

	@Test
	public void testRunInOutputPhase() throws Exception {
		GenericHookScriptRunner processor = new GenericHookScriptRunner(Hook.post_datafetch, Phase.Output);
		
		processor.process(st, null);
		processor.fetchData(st);
		Assert.assertEquals("Test query", st.getQuestion().getQuery());
		Assert.assertNull(st.getQuestion().getCnClickedCluster());

		processor.process(st);
		
		Assert.assertEquals("Test query", st.getQuestion().getQuery());
		Assert.assertEquals("Clicked Cluster", st.getQuestion().getCnClickedCluster());		
	}

}
