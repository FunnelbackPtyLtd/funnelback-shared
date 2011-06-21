package com.funnelback.contentoptimiser;

import com.funnelback.contentoptimiser.DocumentContentScoreBreakdown;

public interface DocumentWordsProcessor {
	
	DocumentContentScoreBreakdown explainQueryTerm(String query);

	String[] getTopFiveWords();

	int totalWords();
	int uniqueWords();

}
