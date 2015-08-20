package com.funnelback.publicui.test.search.model.collection.facetednavigation;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition.MetadataAndValue;

public class CategoryDefinitionTests {

    @Test
    public void testParseMetadata() {
        MetadataAndValue mv = CategoryDefinition.parseMetadata("a:bcd");
        Assert.assertEquals("a", mv.metadata);
        Assert.assertEquals("bcd", mv.value);
        
        mv = CategoryDefinition.parseMetadata("a:bcd efg");
        Assert.assertEquals("a", mv.metadata);
        Assert.assertEquals("bcd efg", mv.value);

        mv = CategoryDefinition.parseMetadata("a:bcd efg\"h");
        Assert.assertEquals("a", mv.metadata);
        Assert.assertEquals("bcd efg\"h", mv.value);

        mv = CategoryDefinition.parseMetadata("X:yz");
        Assert.assertEquals("X", mv.metadata);
        Assert.assertEquals("yz", mv.value);
        
        mv = CategoryDefinition.parseMetadata("J k:lm no");
        Assert.assertEquals("J k", mv.metadata);
        Assert.assertEquals("lm no", mv.value);
        
        mv = CategoryDefinition.parseMetadata(":value");
        Assert.assertEquals("", mv.metadata);
        Assert.assertEquals("value", mv.value);
        
        mv = CategoryDefinition.parseMetadata("a:");
        Assert.assertEquals("a", mv.metadata);
        Assert.assertEquals("", mv.value);
        
        mv = CategoryDefinition.parseMetadata("");
        Assert.assertEquals(null, mv.metadata);
        Assert.assertEquals(null, mv.value);

        mv = CategoryDefinition.parseMetadata(null);
        Assert.assertEquals(null, mv.metadata);
        Assert.assertEquals(null, mv.metadata);

        mv = CategoryDefinition.parseMetadata("-a:");
        Assert.assertEquals("-a", mv.metadata);
        Assert.assertEquals("", mv.value);

    }
    
}
