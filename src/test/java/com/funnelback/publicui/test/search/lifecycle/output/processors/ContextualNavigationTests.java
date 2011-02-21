package com.funnelback.publicui.test.search.lifecycle.output.processors;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.impl.StaxStreamParser;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.ContextualNavigation;
import com.funnelback.publicui.search.model.padre.Category;
import com.funnelback.publicui.search.model.padre.Cluster;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.xml.XmlParsingException;

public class ContextualNavigationTests {

	private SearchTransaction st;

	@Before
	public void before() throws XmlParsingException, IOException {
		SearchResponse response = new SearchResponse();
		response.setResultPacket(new StaxStreamParser().parse(FileUtils.readFileToString(new File(
				"test_data/padre-xml/complex.xml"))));

		st = new SearchTransaction(new SearchQuestion(), response);
		st.getQuestion().setQuery("CN test");

	}

	@Test
	public void testMissingData() throws OutputProcessorException {
		ContextualNavigation processor = new ContextualNavigation();

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
	}

	@Test
	public void test() throws OutputProcessorException, UnsupportedEncodingException {
		ContextualNavigation processor = new ContextualNavigation();
		processor.process(st);
		
		com.funnelback.publicui.search.model.padre.ContextualNavigation cn = st.getResponse().getResultPacket().getContextualNavigation();
		Assert.assertEquals(3, cn.getCategories().size());
		
		Assert.assertTrue(cn.getCategories().get(2).getMoreLink().startsWith("?"));
		Assert.assertFalse(cn.getCategories().get(2).getMoreLink().contains("/"));
		Assert.assertNull(cn.getCategories().get(2).getFewerLink());
		
		Assert.assertTrue(cn.getCategories().get(1).getFewerLink().startsWith("?"));
		Assert.assertFalse(cn.getCategories().get(1).getFewerLink().contains("/"));
		
		for (Category cat : cn.getCategories()) {
			for (Cluster cluster: cat.getClusters()) {
				String query = cluster.getQuery();
				if (query != null) {
					query = query.replaceAll("%60", "");
					Assert.assertTrue(cluster.getHref().contains("clicked_fluster=" + URLEncoder.encode(query.toLowerCase(), "UTF-8")));
				}
				Assert.assertTrue(cluster.getHref().contains("cluster0=CN+test"));
				Assert.assertFalse(cluster.getHref().contains("cluster1"));
			}
		}
	}
	
	@Test
	public void testSelectedFacet() throws OutputProcessorException, UnsupportedEncodingException {
		st.getQuestion().setCnClickedCluster("previous cluster");
		st.getQuestion().getCnPreviousClusters().add("previous previous cluster");
		ContextualNavigation processor = new ContextualNavigation();
		processor.process(st);
		
		com.funnelback.publicui.search.model.padre.ContextualNavigation cn = st.getResponse().getResultPacket().getContextualNavigation();
		Assert.assertEquals(3, cn.getCategories().size());
		
		for (Category cat : cn.getCategories()) {
			for (Cluster cluster: cat.getClusters()) {
				String query = cluster.getQuery();
				if (query != null) {
					query = query.replaceAll("%60", "");
					Assert.assertTrue(cluster.getHref().contains("clicked_fluster=" + URLEncoder.encode(query.toLowerCase(), "UTF-8")));
				}
				Assert.assertTrue(cluster.getHref().contains("cluster1=previous+cluster"));
				Assert.assertTrue(cluster.getHref().contains("cluster0=previous+previous+cluster"));
			}
		}

	}

}
