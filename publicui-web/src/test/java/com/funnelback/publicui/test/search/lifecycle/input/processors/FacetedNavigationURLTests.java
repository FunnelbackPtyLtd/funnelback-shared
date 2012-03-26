package com.funnelback.publicui.test.search.lifecycle.input.processors;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.publicui.search.lifecycle.input.processors.FacetedNavigation;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.config.AbstractLocalConfigRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class FacetedNavigationURLTests {

	@Resource(name="localConfigRepository")
	private AbstractLocalConfigRepository configRepository;
	
	private FacetedNavigation processor;
	private SearchTransaction st;

	@Before
	public void before() {
		SearchQuestion question = new SearchQuestion();
		question.setCollection(configRepository.getCollection("faceted-navigation-urls"));
		st = new SearchTransaction(question, null);
		
		processor = new FacetedNavigation();
	}
	
	@Test
	public void test() {		
		processor.processInput(st);
		
		Assert.assertEquals(1, st.getQuestion().getDynamicQueryProcessorOptions().size());
		Assert.assertEquals("-count_urls 0", st.getQuestion().getDynamicQueryProcessorOptions().get(0));
	}
	
	@Test
	public void testFacetSelected() {
		Assert.assertEquals(0, st.getQuestion().getFacetsQueryConstraints().size());
		Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
		
		st.getQuestion().getRawInputParameters().put("f.By URL|url", new String[] {"Shakespeare/cleopatra"});
		processor.processInput(st);
		
		Assert.assertNull(st.getQuestion().getFacetsGScopeConstraints());
		Assert.assertEquals(1, st.getQuestion().getFacetsQueryConstraints().size());
		Assert.assertEquals("|v:\"Shakespeare/cleopatra\"", st.getQuestion().getFacetsQueryConstraints().get(0));
		
		
		
		
	}
	
	
}
