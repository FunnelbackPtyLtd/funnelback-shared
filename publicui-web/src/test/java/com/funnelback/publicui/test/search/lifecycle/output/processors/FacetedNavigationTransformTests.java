package com.funnelback.publicui.test.search.lifecycle.output.processors;

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;

import java.io.FileNotFoundException;
import java.util.Date;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.EnvironmentVariableException;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.FacetedNavigationTransform;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryType;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.ConfigRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class FacetedNavigationTransformTests {

	@Autowired
	private FacetedNavigationTransform processor;
	
	@Resource(name="localConfigRepository")
	private ConfigRepository configRepository;
	
	private SearchTransaction st;
	
	private Class<Script> transformScriptClass;
	
	@Before
	public void before() {
		CategoryType ct = new CategoryType("Category Type");
		ct.getCategories().add(new Facet.Category("value1", "category1", 5, "a=b"));
		ct.getCategories().add(new Facet.Category("value2", "category2", 10, "c=d"));
		
		Facet f = new Facet("Test Facet");
		f.getCategoryTypes().add(ct);
		
		SearchResponse sr = new SearchResponse();
		sr.getFacets().add(f);
		
		SearchQuestion sq = new SearchQuestion();
		sq.setCollection(configRepository.getCollection("faceted-navigation"));
		st = new SearchTransaction(sq, sr);
		
		// Backup transform script
		transformScriptClass = sq.getCollection().getFacetedNavigationConfConfig().getTransformScriptClass();
	}
	
	@Test
	public void testMissingData() throws OutputProcessorException {
		// No transaction
		processor.process(null);

		// No response
		processor.process(new SearchTransaction(null, null));

		// No results
		SearchResponse response = new SearchResponse();
		processor.process(new SearchTransaction(null, response));

		// No results in packet
		response.setResultPacket(new ResultPacket());
		processor.process(new SearchTransaction(null, response));
		
		// No facets
		ResultPacket rp = new ResultPacket();
		rp.getResults().add(new Result(new Integer(0),
				new Integer(0),
				"title",
				"collection",
				new Integer(0),
				"liveUrl",
				"summary",
				"cacheUrl",
				new Date(),
				new Integer(0), 
				"pdf",
				new Integer(0),
				new Integer(0),
				null,
				null, null, null));
		response.setResultPacket(rp);
		processor.process(new SearchTransaction(null, response));
	}
	
	@Test
	public void testScript() throws FileNotFoundException, EnvironmentVariableException, OutputProcessorException {
		st.getQuestion().getCollection().getFacetedNavigationConfConfig().setTransformScriptClass(transformScriptClass);
		processor.process(st);

		Assert.assertEquals(1, st.getResponse().getFacets().size());
		Assert.assertEquals("Test Facet (Groovy)", st.getResponse().getFacets().get(0).getName());
		Assert.assertEquals("Category Type", st.getResponse().getFacets().get(0).getCategoryTypes().get(0).getLabel());
	}

	@Test
	public void testNoScript() throws OutputProcessorException {
		st.getQuestion().getCollection().getFacetedNavigationConfConfig().setTransformScriptClass(null);
		processor.process(st);

		Assert.assertEquals(1, st.getResponse().getFacets().size());
		Assert.assertEquals("Test Facet", st.getResponse().getFacets().get(0).getName());
		Assert.assertEquals("Category Type", st.getResponse().getFacets().get(0).getCategoryTypes().get(0).getLabel());
	}

	@Test
	public void testInvalidScript() throws OutputProcessorException {
		@SuppressWarnings("unchecked")
		Class<Script> s = new GroovyClassLoader().parseClass("throw new RuntimeException()");
		st.getQuestion().getCollection().getFacetedNavigationConfConfig().setTransformScriptClass(s);
		processor.process(st);

		Assert.assertEquals(1, st.getResponse().getFacets().size());
		Assert.assertEquals("Test Facet", st.getResponse().getFacets().get(0).getName());
		Assert.assertEquals("Category Type", st.getResponse().getFacets().get(0).getCategoryTypes().get(0).getLabel());	
	}
	
}
