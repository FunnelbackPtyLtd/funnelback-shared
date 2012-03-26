package com.funnelback.publicui.test.search.lifecycle.output.processors;

import java.io.File;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.impl.StaxStreamParser;
import com.funnelback.publicui.search.lifecycle.output.processors.FacetedNavigation;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.config.AbstractLocalConfigRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class FacetedNavigationQueryTests {

	@Resource(name="localConfigRepository")
	private AbstractLocalConfigRepository configRepository;

	private SearchTransaction st;
	private FacetedNavigation processor;
	
	@Before
	public void before() throws Exception {
		SearchQuestion question = new SearchQuestion();
		question.setCollection(configRepository.getCollection("faceted-navigation-query"));
		
		st = new SearchTransaction(question, new SearchResponse());
		
		processor = new FacetedNavigation();
	}
	
	@Test
	public void testMissingData() throws Exception {
		// No transaction
		processor.processOutput(null);
		
		// No response
		processor.processOutput(new SearchTransaction(null, null));
		
		// No results
		SearchResponse response = new SearchResponse();
		processor.processOutput(new SearchTransaction(null, response));
		
		// No results in packet
		response.setResultPacket(new ResultPacket());
		processor.processOutput(new SearchTransaction(null, response));
		
		// No collection
		processor.processOutput(new SearchTransaction(new SearchQuestion(), response));
		
		// Collection but no config
		SearchQuestion sq = new SearchQuestion();
		sq.setCollection(new Collection("dummy", null));
		processor.processOutput(new SearchTransaction(sq, response));
		
		// Config but no faceted_nav. config
		sq = new SearchQuestion();
		sq.setCollection(new Collection("dummy", new NoOptionsConfig("dummy")));
		processor.processOutput(new SearchTransaction(sq, response));
	}
	
	@Test
	public void test() throws Exception {
		
		st.getResponse().setResultPacket(new StaxStreamParser().parse(FileUtils.readFileToString(new File("src/test/resources/padre-xml/faceted-navigation-query.xml"))));
		
		Assert.assertEquals(0, st.getResponse().getFacets().size());
		processor.processOutput(st);
		
		Assert.assertEquals(1, st.getResponse().getFacets().size());
		
		Facet f = st.getResponse().getFacets().get(0);
		Assert.assertEquals("By Query", f.getName());
		Assert.assertEquals(2, f.getCategories().size());

		// First category: Managers, gscope=1
		Facet.Category c = f.getCategories().get(0);
		Assert.assertNull(c.getLabel());
		Assert.assertEquals("f.By Query|2", c.getQueryStringParamName());
		Assert.assertEquals(1, c.getValues().size());
		
		Facet.CategoryValue cv = c.getValues().get(0);
		Assert.assertEquals("2", cv.getConstraint());
		Assert.assertEquals(630, cv.getCount());
		Assert.assertEquals("2", cv.getData());
		Assert.assertEquals("Managers", cv.getLabel());
		Assert.assertEquals("f.By Query|2=Managers", cv.getQueryStringParam());
		
		// No sub-categories should be returned since nothing
		// has been selected in the first level category
		Assert.assertEquals(0, c.getCategories().size());
		
		// Second category: Plumbers, gscope=2
		c = f.getCategories().get(1);
		Assert.assertNull(c.getLabel());
		Assert.assertEquals("f.By Query|1", c.getQueryStringParamName());
		Assert.assertEquals(1, c.getValues().size());
		
		cv = c.getValues().get(0);
		Assert.assertEquals("1", cv.getConstraint());
		Assert.assertEquals(27, cv.getCount());
		Assert.assertEquals("1", cv.getData());
		Assert.assertEquals("Plumbers", cv.getLabel());
		Assert.assertEquals("f.By Query|1=Plumbers", cv.getQueryStringParam());
		
		Assert.assertEquals(0, c.getCategories().size());
		
	}
		
}
