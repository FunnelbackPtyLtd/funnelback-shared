package com.funnelback.publicui.test.search.lifecycle.output.processors;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.impl.StaxStreamParser;
import com.funnelback.publicui.search.lifecycle.output.processors.FacetedNavigation;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.xml.XmlParsingException;

public class FacetedNavigationTests {

	private SearchTransaction st;
	
	@Before
	public void before() throws XmlParsingException, IOException {
		SearchResponse response = new SearchResponse();
		response.setResultPacket(new StaxStreamParser().parse(FileUtils.readFileToString(new File("src/test/resources/padre-xml/complex.xml"))));
		
		st = new SearchTransaction(null, response);

	}
	
	@Test
	public void testMissingData() {
		FacetedNavigation processor = new FacetedNavigation();
		
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
	public void test() {
		//FIXME needs proper facet implementation
	}
	
}
