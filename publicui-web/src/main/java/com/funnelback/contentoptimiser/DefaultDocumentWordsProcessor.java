package com.funnelback.contentoptimiser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

public class DefaultDocumentWordsProcessor implements DocumentWordsProcessor {

	private final Map<String,Map<String,Integer>> countByTerms;
	private final Multiset<Entry<String,Integer>> termsSortedByFrequency;
	private final int totalWordCount;
	
	public DefaultDocumentWordsProcessor(String wordsInDocument) {
		countByTerms = new HashMap<String,Map<String,Integer>>(); 
		int count = 0;
		String[] words = wordsInDocument.split("\\s+");
		for(String word : words){
			if(word.indexOf('_') != -1) {
				String[] wordAndFieldType = word.split("_");
				String fieldName = wordAndFieldType[1];
				if(! countByTerms.containsKey(fieldName)) countByTerms.put(fieldName, new HashMap<String,Integer>());
				if(! countByTerms.get(fieldName).containsKey(wordAndFieldType[0])) countByTerms.get(fieldName).put(wordAndFieldType[0],0);
				countByTerms.get(fieldName).put(wordAndFieldType[0],(countByTerms.get(fieldName).get(wordAndFieldType[0])+1));
				
			} else {
				if(! countByTerms.containsKey("_")) countByTerms.put("_", new HashMap<String,Integer>());
				if(! countByTerms.get("_").containsKey(word)) countByTerms.get("_").put(word,0);
				countByTerms.get("_").put(word,(countByTerms.get("_").get(word)+1));
				count++;
			}
		}
		totalWordCount = count;

		termsSortedByFrequency = TreeMultiset.create(new Comparator<Entry<String,Integer>>() {
			@Override
			public int compare(Entry<String,Integer> arg0, Entry<String,Integer> arg1) {
				int ret =arg1.getValue().compareTo(arg0.getValue());
				if(ret == 0) {
					return arg1.getKey().compareTo(arg0.getKey());
				}
				return ret;
			}
		});
		
		for(Entry<String,Integer> e : countByTerms.get("_").entrySet()) {
			termsSortedByFrequency.add(e);
		}
	}
	
	@Override
	public String[] getTopFiveWords() {
		int i = 0;
		List<String> topFive = new ArrayList<String>(5);
		for(Entry<String,Integer> e : termsSortedByFrequency) {
			if(i == 5) break;
			i++;
			topFive.add(e.getKey());
		}
		return topFive.toArray(new String[0]);
	}

	@Override
	public DocumentContentScoreBreakdown explainQueryTerm(String query) {
		int count = countByTerms.get("_").get(query); 
		
		int countTermsLess = 0;
		for(Entry<String,Integer> e : termsSortedByFrequency) {
			if(e.getValue() < count) countTermsLess++;
		}
		int percentageLess = Math.round((float) countTermsLess/(float)(termsSortedByFrequency.size()-1) *(float)100);
		
		Map<String,Integer> m = new HashMap<String,Integer>();
		for(Entry<String,Map<String,Integer>> e : countByTerms.entrySet()) {
			if(e.getValue().containsKey(query) && ! e.getKey().equals("_")) {
				m.put(e.getKey(),e.getValue().get(query));
			}
		}
		
		return new DocumentContentScoreBreakdown(count,percentageLess,m);
	}

	@Override
	public int totalWords() {
		return totalWordCount;
	}

	@Override
	public int uniqueWords() {
		return termsSortedByFrequency.size();
	}

}
