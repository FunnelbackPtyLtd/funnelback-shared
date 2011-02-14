package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.io.FileNotFoundException;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.EnvironmentVariableException;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.input.processors.FacetedNavigation;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.config.AbstractLocalConfigRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:test_data/spring/applicationContext.xml")
@Ignore
public class FacetedNavigationTests {

	@Resource(name="localConfigRepository")
	private AbstractLocalConfigRepository configRepository;
	
	@Test
	public void testMissingData() throws FileNotFoundException, EnvironmentVariableException {
		FacetedNavigation processor = new FacetedNavigation();
		
		// No transaction
		processor.process(null, null);
		
		// No question
		SearchTransaction st = new SearchTransaction(null, null);
		processor.process(st, null);
		Assert.assertNull(st.getQuestion());
		
		// No collection
		SearchQuestion question = new SearchQuestion();
		st = new SearchTransaction(question, null);
		processor.process(st, null);
		Assert.assertEquals(0, st.getQuestion().getDynamicQueryProcessorOptions().size());
		
		// No faceted navigation config
		question.setCollection(new Collection("dummy", new NoOptionsConfig("dummy")));
		st = new SearchTransaction(question, null);
		processor.process(st, null);
		Assert.assertEquals(0, st.getQuestion().getDynamicQueryProcessorOptions().size());
		
		// No QP Options
		Collection c = new Collection("dummy", new NoOptionsConfig("dummy"));
		c.setFacetedNavigationLiveConfig(new FacetedNavigationConfig(null, null));
		question.setCollection(c);
		st = new SearchTransaction(question, null);
		processor.process(st, null);
		Assert.assertEquals(0, st.getQuestion().getDynamicQueryProcessorOptions().size());

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
