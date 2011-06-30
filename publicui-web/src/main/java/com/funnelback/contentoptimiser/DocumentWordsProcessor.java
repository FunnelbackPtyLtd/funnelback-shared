package com.funnelback.contentoptimiser;

import java.util.List;

import com.funnelback.contentoptimiser.DocumentContentScoreBreakdown;

public interface DocumentWordsProcessor {
	
	DocumentContentScoreBreakdown explainQueryTerm(String query);

	String[] getTopFiveWords(List<String> stopWords, String fieldType);

	int totalWords();
	int uniqueWords();

}
