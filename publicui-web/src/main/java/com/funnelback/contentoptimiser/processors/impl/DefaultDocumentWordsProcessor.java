package com.funnelback.contentoptimiser.processors.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.funnelback.contentoptimiser.SingleTermFrequencies;
import com.funnelback.contentoptimiser.processors.DocumentWordsProcessor;
import com.funnelback.publicui.search.model.anchors.AnchorDescription;
import com.funnelback.publicui.search.model.anchors.AnchorModel;
import com.funnelback.publicui.search.model.collection.Collection;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultiset;


public class DefaultDocumentWordsProcessor implements DocumentWordsProcessor {

    private final Map<String,Map<String,Integer>> countByTerms;
    private final Multiset<Entry<String,Integer>> termsSortedByFrequency;
    private final int totalWordCount;
    

    public DefaultDocumentWordsProcessor(String wordsInDocument, AnchorModel anchors, SetMultimap<String,String> stemMatches) {
        countByTerms = new HashMap<String,Map<String,Integer>>(); 
        int count = 0;
        String[] words = wordsInDocument.split("\\s+");
    
        for(String word : words){
            if(word.indexOf('_') != -1) {
                String[] wordAndFieldType = word.split("_");
                String fieldName = wordAndFieldType[1];
                String singleWord = wordAndFieldType[0];
                
                countWord(singleWord, fieldName);
                if(stemMatches.containsKey(singleWord)) {
                    for(String equiv : stemMatches.get(singleWord)) {
                        countWord(equiv, fieldName);
                    }
                }
            } else {
                String fieldName = "_";
                countWord(word, fieldName);
                count++;
                if(stemMatches.containsKey(word)) {
                    for(String equiv : stemMatches.get(word)) {
                        countWord(equiv, fieldName);
                    }
                }
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
        
        if(countByTerms.get("_") != null) {
            for(Entry<String,Integer> e : countByTerms.get("_").entrySet()) {
                termsSortedByFrequency.add(e);
            }
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
    public String[] getCommonWords(List<String> stopWords,String fieldType) {
        int i = 0;
        Set<String> stopSet = new HashSet<String>(stopWords);
        
        List<String> topTen = new ArrayList<String>(10);
        for(Entry<String,Integer> e : termsSortedByFrequency) {
            if(i == 10) break;
            if(! stopSet.contains(e.getKey().toLowerCase())) {
                topTen.add(e.getKey());
                i++;
            }
        }
        return topTen.toArray(new String[0]);
    }

    @Override
    public SingleTermFrequencies explainQueryTerm(String queryTerm, Collection collection) {
        Map<String, Integer> plainDictionary = countByTerms.get("_");
        if(plainDictionary == null) {
            // the document doesn't contain any body words!
            return new SingleTermFrequencies(0,0,buildSingleTermMap(queryTerm));
        }
        
        Integer count = plainDictionary.get(queryTerm);
        
        if(count == null) {
            // the document doesn't contain this word in the body text!
            return new SingleTermFrequencies(0,0,buildSingleTermMap(queryTerm));
        }
        
        int countTermsLess = 0;
        for(Entry<String,Integer> e : termsSortedByFrequency) {
            if(e.getValue() < count) countTermsLess++;
        }
        int percentageLess = Math.round((float) countTermsLess/(float)(termsSortedByFrequency.size()-1) *(float)100);
        
        Map<String, Integer> m = buildSingleTermMap(queryTerm);
    
        return new SingleTermFrequencies(count,percentageLess,m);
    }

    private Map<String, Integer> buildSingleTermMap(String query) {
        Map<String,Integer> m = new HashMap<String,Integer>();
        for(Entry<String,Map<String,Integer>> e : countByTerms.entrySet()) {
            if(e.getValue().containsKey(query)) {
                m.put(e.getKey(),e.getValue().get(query));
            }
        }
        return m;
    }

    @Override
    public int getTotalWords() {
        return totalWordCount;
    }

    @Override
    public int setUniqueWords() {
        return termsSortedByFrequency.size();
    }

}
