package com.funnelback.contentoptimiser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.funnelback.publicui.search.model.anchors.AnchorDescription;
import com.funnelback.publicui.search.model.anchors.AnchorModel;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

public class DefaultDocumentWordsProcessor implements DocumentWordsProcessor {

	private final Map<String,Map<String,Integer>> countByTerms;
	private final Multiset<Entry<String,Integer>> termsSortedByFrequency;
	private final int totalWordCount;
	
	public DefaultDocumentWordsProcessor(String wordsInDocument, AnchorModel anchors) {
		countByTerms = new HashMap<String,Map<String,Integer>>(); 
		int count = 0;
		String[] words = wordsInDocument.split("\\s+");
	
		for(String word : words){
			if(word.indexOf('_') != -1) {
				String[] wordAndFieldType = word.split("_");
				String fieldName = wordAndFieldType[1];
				String singleWord = wordAndFieldType[0];
				
				countWord(singleWord, fieldName);
			} else {
				String fieldName = "_";
				countWord(word, fieldName);
				count++;
			}
		}
		
		for(AnchorDescription anchor : anchors.getAnchors()) {
			String[] anchorWords = anchor.getAnchorText().split("\\s+");
			int occurences = anchor.getTotalLinkCount();
			for(String word : anchorWords) {
				if(anchor.getLinkType().equals("K")) {
					for(int i = 0; i < occurences; i++) countWord(word,"K");
				} else {
					for(int i = 0; i < occurences; i++) countWord(word,"k");
				}
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

	private void countWord(String singleWord, String fieldName) {
		// Make sure we have a dictionary for this field type
		if(! countByTerms.containsKey(fieldName)) countByTerms.put(fieldName, new HashMap<String,Integer>());
		// Make sure we have an entry in the dictionary for this field type
		if(! countByTerms.get(fieldName).containsKey(singleWord)) countByTerms.get(fieldName).put(singleWord,0);
		// Increment the entry
		countByTerms.get(fieldName).put(singleWord,(countByTerms.get(fieldName).get(singleWord)+1));
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
		Map<String, Integer> plainDictionary = countByTerms.get("_");
		if(plainDictionary == null) {
			// the document doesn't contain any body words!
			return new DocumentContentScoreBreakdown(0,0,buildSingleTermMap(query));
		}
		
		Integer count = plainDictionary.get(query);
		
		if(count == null) {
			// the document doesn't contain this word in the body text!
			return new DocumentContentScoreBreakdown(0,0,buildSingleTermMap(query));
		}
		
		int countTermsLess = 0;
		for(Entry<String,Integer> e : termsSortedByFrequency) {
			if(e.getValue() < count) countTermsLess++;
		}
		int percentageLess = Math.round((float) countTermsLess/(float)(termsSortedByFrequency.size()-1) *(float)100);
		
		Map<String, Integer> m = buildSingleTermMap(query);
		
		return new DocumentContentScoreBreakdown(count,percentageLess,m);
	}

	private Map<String, Integer> buildSingleTermMap(String query) {
		Map<String,Integer> m = new HashMap<String,Integer>();
		for(Entry<String,Map<String,Integer>> e : countByTerms.entrySet()) {
			if(e.getValue().containsKey(query) && ! e.getKey().equals("_")) {
				m.put(e.getKey(),e.getValue().get(query));
			}
		}
		return m;
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
