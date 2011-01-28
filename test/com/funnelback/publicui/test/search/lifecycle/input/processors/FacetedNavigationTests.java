package com.funnelback.publicui.test.search.lifecycle.input.processors;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.publicui.search.lifecycle.input.processors.FacetedNavigation;
import com.funnelback.publicui.search.model.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.impl.LocalConfigRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:test_data/spring/applicationContext.xml")
public class FacetedNavigationTests {

	@Resource(name="localConfigRepository")
	private LocalConfigRepository configRepository;
	
	@Test
	public void testMissingData() {
		FacetedNavigation processor = new FacetedNavigation();
		
		// No transaction
		processor.process(null, null);
		
		// No question
		processor.process(new SearchTransaction(null, null), null);
		
		// No collection
		SearchQuestion question = new SearchQuestion();
		processor.process(new SearchTransaction(question, null), null);
		
		// No faceted navigation config
		question.setCollection(new Collection("dummy", null));
		processor.process(new SearchTransaction(question, null), null);
	}
	
	@Test
	public void test() {
		Collection c = configRepository.getCollection("faceted-navigation");
		
		SearchQuestion question = new SearchQuestion();
		question.setCollection(c);
		SearchTransaction st = new SearchTransaction(question, null);
		
		FacetedNavigation processor = new FacetedNavigation();
		
		processor.process(st, null);
		
		Assert.assertEquals(1, st.getQuestion().getDynamicQueryProcessorOptions().size());
		Assert.assertEquals("-rmcfdt", st.getQuestion().getDynamicQueryProcessorOptions().get(0));
	}
	
	
}
