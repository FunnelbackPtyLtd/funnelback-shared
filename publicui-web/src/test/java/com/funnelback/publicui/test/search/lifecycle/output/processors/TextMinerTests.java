package com.funnelback.publicui.test.search.lifecycle.output.processors;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.TextMiner;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.EntityDefinition;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.test.mock.MockTextMinerService;
import com.funnelback.publicui.xml.XmlParsingException;

public class TextMinerTests {

	private TextMiner processor;	
	private SearchTransaction st;
	
	@Before
	public void before() throws IOException, XmlParsingException {
		Collection c = new Collection("dummy", new NoOptionsConfig("dummy"));
		c.getConfiguration().setValue("text_miner_enabled", "true");
		
		SearchQuestion question = new SearchQuestion();
		question.setQuery("ABC");
		question.setCollection(c);
		
		SearchResponse response = new SearchResponse();

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
	
	@Test
	public void testBlacklistDoesntMatch() throws OutputProcessorException {
		st.getQuestion().getCollection().getTextMinerBlacklist().add("DEF");
		processor.processOutput(st);
		
		EntityDefinition entityDefinition = st.getResponse().getEntityDefinition();
		Assert.assertEquals("Missing or incorrect definition", "is the Australian Broadcasting Corporation", entityDefinition.getDefinition());
		Assert.assertEquals("Missing source URL", "http://www.abc.net.au/", entityDefinition.getUrl());
	}
	
	@Test
	public void testBlacklistMatches() throws OutputProcessorException {
		st.getQuestion().getCollection().getTextMinerBlacklist().add("abc");
		processor.processOutput(st);
		
		EntityDefinition entityDefinition = st.getResponse().getEntityDefinition();
		Assert.assertNull(entityDefinition);
	}

	
	@SuppressWarnings("unchecked")
	@Test
	public void testNounPhrases() throws OutputProcessorException {
		ResultPacket rp = new ResultPacket();
		rp.getResults().add(new Result(null, null, "", "", null, "", "", "", null, null, "", null, null, null, null, null, "", "", null, ""));
		rp.getResults().add(new Result(null, null, "", "", null, "", "", "", null, null, "", null, null, null, null, null, "", "", null, ""));
		st.getResponse().setResultPacket(rp);
		
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
