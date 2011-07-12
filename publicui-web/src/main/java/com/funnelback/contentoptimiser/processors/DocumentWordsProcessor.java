package com.funnelback.contentoptimiser.processors;

import java.util.List;

import com.funnelback.contentoptimiser.SingleTermFrequencies;
import com.funnelback.publicui.search.model.collection.Collection;

public interface DocumentWordsProcessor {
	
	SingleTermFrequencies explainQueryTerm(String query, Collection collection);

	String[] getCommonWords(List<String> stopWords, String fieldType);

	int getTotalWords();
	int setUniqueWords();

}
