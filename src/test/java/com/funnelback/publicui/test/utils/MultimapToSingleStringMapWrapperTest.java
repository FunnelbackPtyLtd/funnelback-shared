package com.funnelback.publicui.test.utils;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.utils.MultimapToSingleStringMapWrapper;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.MultimapBuilder.ListMultimapBuilder;

public class MultimapToSingleStringMapWrapperTest {

    @Test
    public void testEmpty() {
        ListMultimap<String, String> underlyingMetadata = ListMultimapBuilder.hashKeys().arrayListValues().build();

        ListMultimap<String, String> underlyingMetadataSeparators = ListMultimapBuilder.hashKeys().arrayListValues().build();

        ListMultimap<String, String> definedMetadataSeparators = ListMultimapBuilder.hashKeys().arrayListValues().build();

        MultimapToSingleStringMapWrapper map = new MultimapToSingleStringMapWrapper(underlyingMetadata, underlyingMetadataSeparators, definedMetadataSeparators);
        
        Assert.assertNull(map.get("foo"));
    }

    @Test
    public void testGet() {
        ListMultimap<String, String> underlyingMetadata = ListMultimapBuilder.hashKeys().arrayListValues().build();
        underlyingMetadata.putAll("foo", Lists.newArrayList("a","b","c","d"));

        ListMultimap<String, String> underlyingMetadataSeparators = ListMultimapBuilder.hashKeys().arrayListValues().build();
        underlyingMetadataSeparators.putAll("foo", Lists.newArrayList("|",";"));

        ListMultimap<String, String> definedMetadataSeparators = ListMultimapBuilder.hashKeys().arrayListValues().build();
        definedMetadataSeparators.putAll("foo", Lists.newArrayList(":","+"));

        MultimapToSingleStringMapWrapper map = new MultimapToSingleStringMapWrapper(underlyingMetadata, underlyingMetadataSeparators, definedMetadataSeparators);
        
        Assert.assertEquals("a|b;c:d", map.get("foo"));
    }

    @Test
    public void testPut() {
        ListMultimap<String, String> underlyingMetadata = ListMultimapBuilder.hashKeys().arrayListValues().build();

        ListMultimap<String, String> underlyingMetadataSeparators = ListMultimapBuilder.hashKeys().arrayListValues().build();

        ListMultimap<String, String> definedMetadataSeparators = ListMultimapBuilder.hashKeys().arrayListValues().build();
        definedMetadataSeparators.putAll("foo", Lists.newArrayList("|","+"));

        MultimapToSingleStringMapWrapper map = new MultimapToSingleStringMapWrapper(underlyingMetadata, underlyingMetadataSeparators, definedMetadataSeparators);

        Assert.assertEquals(null, map.get("foo"));

        map.put("foo", "bar+goo|gar");
        
        Assert.assertEquals("bar+goo|gar", map.get("foo"));
        Assert.assertEquals(Lists.newArrayList("bar","goo","gar"), underlyingMetadata.get("foo"));
        Assert.assertEquals(Lists.newArrayList("+","|"), underlyingMetadataSeparators.get("foo"));
    }

}
