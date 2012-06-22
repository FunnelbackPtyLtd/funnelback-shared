package com.funnelback.publicui.test.mock;

import java.util.ArrayList;
import java.util.List;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.EntityDefinition;
import com.funnelback.publicui.search.service.TextMiner;

public class MockTextMinerService implements TextMiner {
	public EntityDefinition getEntityDefinition(String entity, Collection collection) {
		return new EntityDefinition(entity, "is the Australian Broadcasting Corporation", "http://www.abc.net.au/");

	}
	
	public EntityDefinition getCustomDefinition(String entity, Collection collection) {
		return new EntityDefinition(entity, "is a custom definition", "http://www.example.com/");
	}
	
	public List<String> getURLNounPhrases(String URL, Collection collection) {
		List<String> nounPhrases = new ArrayList<String>();
		
		nounPhrases.add("Noun One");
		nounPhrases.add("Noun Two");
		
		return nounPhrases;
	}
}
