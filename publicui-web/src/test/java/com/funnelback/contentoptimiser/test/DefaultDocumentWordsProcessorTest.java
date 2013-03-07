package com.funnelback.contentoptimiser.test;

import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.funnelback.contentoptimiser.SingleTermFrequencies;
import com.funnelback.contentoptimiser.processors.DocumentWordsProcessor;
import com.funnelback.contentoptimiser.processors.impl.DefaultDocumentWordsProcessor;
import com.funnelback.publicui.search.model.anchors.AnchorDescription;
import com.funnelback.publicui.search.model.anchors.AnchorModel;
import com.funnelback.publicui.search.model.collection.Collection;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;


public class DefaultDocumentWordsProcessorTest {

    private AnchorModel anchors;
    
    @Before
    public void setupAnchors() {
        anchors = new AnchorModel();
        anchors.setAnchors(new ArrayList<AnchorDescription>());
    }
    
    @Test
    public void testObtainContent() {
        AnchorDescription anchorDescription = new AnchorDescription("[k1]five text");
        anchorDescription.linkTo("0");
        anchorDescription.linkTo("1");
        anchorDescription.linkTo("2");
        
        AnchorDescription anchorDescription2 = new AnchorDescription("[k0]five text");
        anchorDescription2.linkTo("0");
        anchorDescription2.linkTo("1");
        
        AnchorDescription anchorDescription3 = new AnchorDescription("[K]anchor five");
        anchorDescription3.linkTo("-1");
        
        anchors.getAnchors().add(anchorDescription);    
        anchors.getAnchors().add(anchorDescription2);
        anchors.getAnchors().add(anchorDescription3);
        
        SetMultimap<String, String> emptyStemMatches = HashMultimap.create();
        DocumentWordsProcessor dwp = new DefaultDocumentWordsProcessor("one two two two three four five five five_t five_h six", anchors,emptyStemMatches);
        
        SingleTermFrequencies content = dwp.explainQueryTerm("five",new Collection("test1", null));
        Assert.assertEquals(content.getCount(), 2);
        Assert.assertEquals(1, content.getCount("t").intValue());
        Assert.assertEquals(1, content.getCount("h").intValue());
        Assert.assertEquals(5, content.getCount("k").intValue());
        Assert.assertEquals(1, content.getCount("K").intValue());
        Assert.assertEquals("Should contain no occurences of unknown fields",0, content.getCount("x").intValue());

        
        Assert.assertEquals(80,content.getPercentageLess());
    }
    
    @Test
    public void testNoContentDocument() {
        SetMultimap<String, String> emptyStemMatches = HashMultimap.create();
        DocumentWordsProcessor dwp = new DefaultDocumentWordsProcessor("",anchors,emptyStemMatches);
        SingleTermFrequencies content = dwp.explainQueryTerm("five",new Collection("test1", null));
        
        Assert.assertEquals(0, content.getCount("x").intValue());
        Assert.assertEquals(0,content.getCount());
        Assert.assertEquals(0,content.getPercentageLess());
    }
    
    @Test
    public void testNoMatchQuery() {
        SetMultimap<String, String> emptyStemMatches = HashMultimap.create();
        DocumentWordsProcessor dwp = new DefaultDocumentWordsProcessor("one two two two three four five five six",anchors,emptyStemMatches);
        SingleTermFrequencies content = dwp.explainQueryTerm("seven",new Collection("test1", null));
        
        Assert.assertEquals(0, content.getCount("x").intValue());
        Assert.assertEquals(0,content.getCount());
        Assert.assertEquals(0,content.getPercentageLess());
    }

    @Test
    public void testStemMatchQuery() {
        SetMultimap<String, String> stemMatches = HashMultimap.create();
        stemMatches.put("ones", "one");
        stemMatches.put("twos", "two");
        DocumentWordsProcessor dwp = new DefaultDocumentWordsProcessor("one two ones two one_t two ones_t three twos twos_t four five five six",anchors,stemMatches);
        SingleTermFrequencies content = dwp.explainQueryTerm("one",new Collection("test1", null));
        
        Assert.assertEquals(2, content.getCount("t").intValue());
        Assert.assertEquals(2,content.getCount());

    }
    
    @Test
    public void testNoContentWords() {
        SetMultimap<String, String> stemMatches = HashMultimap.create();
        stemMatches.put("ones", "one");
        stemMatches.put("twos", "two");
        DocumentWordsProcessor dwp = new DefaultDocumentWordsProcessor("one_t  ones_t  twos_t",anchors,stemMatches);
        SingleTermFrequencies content = dwp.explainQueryTerm("one",new Collection("test1", null));
        
        Assert.assertEquals(2, content.getCount("t").intValue());
        Assert.assertEquals(0,content.getCount());

    }
    
    @Test
    public void testDocumentOverview() {
        SetMultimap<String, String> emptyStemMatches = HashMultimap.create();
        DocumentWordsProcessor dwp = new DefaultDocumentWordsProcessor("one two two two three four five five six",anchors,emptyStemMatches);
        String[] expectedFive =new String[] {"two","five","three","six","one"};
        Assert.assertTrue("Top five words in document incorrect ", Arrays.equals(expectedFive, dwp.getCommonWords(new ArrayList<String>(),"_")));
        Assert.assertEquals(9,dwp.getTotalWords());
        Assert.assertEquals(6, dwp.setUniqueWords());
    }
    
}

