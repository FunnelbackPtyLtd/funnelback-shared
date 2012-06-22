package com.funnelback.publicui.test.search.lifecycle.output.processors;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.xml.impl.StaxStreamParser;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.TextMiner;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.EntityDefinition;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.config.AbstractLocalConfigRepository;
import com.funnelback.publicui.test.mock.MockTextMinerService;
import com.funnelback.publicui.xml.XmlParsingException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class TextMinerTests {

	private TextMiner processor;	
	private SearchTransaction st;
	
	@Resource(name="localConfigRepository")
	private AbstractLocalConfigRepository configRepository;
	
	@Before
	public void before() throws IOException, XmlParsingException {
		SearchQuestion question = new SearchQuestion();
		question.setQuery("ABC");
		question.setCollection(configRepository.getCollection("text-miner"));
		
		SearchResponse response = new SearchResponse();
		response.setResultPacket(new StaxStreamParser().parse(FileUtils.readFileToString(new File("src/test/resources/padre-xml/htmlencode-summaries.xml"))));

		st = new SearchTransaction(question, response);
		processor = new TextMiner();
		processor.setTextMiner(new MockTextMinerService());
	}
	
	@Test
	public void testMissingData() throws OutputProcessorException {
		TextMiner processor = new TextMiner();

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
	}
	
	@Test
	public void testEntityDefiniton() throws OutputProcessorException {
		processor.processOutput(st);

		EntityDefinition entityDefinition = st.getResponse().getEntityDefinition();
		
		Assert.assertEquals("Missing or incorrect definition", "is the Australian Broadcasting Corporation", entityDefinition.getDefinition());
		Assert.assertEquals("Missing source URL", "http://www.abc.net.au/", entityDefinition.getUrl());

	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testNounPhrases() throws OutputProcessorException {
		processor.processOutput(st);

		for (Result result : st.getResponse().getResultPacket().getResults()) {
		    List<String> nounPhrases = (List<String>) result.getCustomData().get("noun_phrases");
			Assert.assertEquals("Missing first noun phrase", "Noun One", nounPhrases.get(0));
			Assert.assertEquals("Missing second noun phrase", "Noun Two", nounPhrases.get(1));			
	    }				
	}
	
	@Test
	public void testCustomDefinitions() throws OutputProcessorException {
		processor.processOutput(st);
		
        String definition = (String) st.getResponse().getCustomData().get("CUSTOM");
		Assert.assertEquals("Missing custom definition", "is a custom definition", definition);
	}
}
