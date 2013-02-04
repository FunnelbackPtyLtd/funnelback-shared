package com.funnelback.publicui.test.search.lifecycle.output.processors;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.HTMLEncodeSummaries;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.xml.XmlParsingException;
import com.funnelback.publicui.xml.padre.StaxStreamParser;

public class HTMLEncodeSummariesTests {

	private SearchTransaction st;
	private HTMLEncodeSummaries processor;
	
	@Before
	public void before() throws XmlParsingException, IOException {
		processor = new HTMLEncodeSummaries();
		
		SearchResponse response = new SearchResponse();
		response.setResultPacket(new StaxStreamParser().parse(FileUtils.readFileToString(new File("src/test/resources/padre-xml/htmlencode-summaries.xml"))));
		
		st = new SearchTransaction(null, response);
	}
	
	@Test
	public void testMissingData() throws Exception{
		// No transaction
		processor.processOutput(null);
		
		// No response & question
		processor.processOutput(new SearchTransaction(null, null));
		
		// No question
		processor.processOutput(new SearchTransaction(null, new SearchResponse()));
		
		// No response
		processor.processOutput(new SearchTransaction(new SearchQuestion(), null));
		
		// No results
		SearchResponse response = new SearchResponse();
		processor.processOutput(new SearchTransaction(null, response));
		
		// No results in packet
		response.setResultPacket(new ResultPacket());
		processor.processOutput(new SearchTransaction(null, response));
	}
	
	@Test
	public void testEncode() throws OutputProcessorException {
		processor.processOutput(st);
		
		Assert.assertEquals(2, st.getResponse().getResultPacket().getResults().size());
		Assert.assertEquals("Summary 1 &#92;&quot;&#39;&lt;&gt;&amp; &amp;amp; éô",
				st.getResponse().getResultPacket().getResults().get(0).getSummary());
		Assert.assertEquals("Summary 2 &#92;&quot;&#39;&lt;&gt;&amp; &amp;amp; éô",
				st.getResponse().getResultPacket().getResults().get(1).getSummary());
	}

	
}
