package com.funnelback.publicui.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder.ListMultimapBuilder;

class MultimapToSingleStringMapWrapperTest {

    @Test
    void testEmpty() {
        ListMultimap<String, String> underlyingMetadata = ListMultimapBuilder.hashKeys().arrayListValues().build();
        ListMultimap<String, String> underlyingMetadataSeparators = ListMultimapBuilder.hashKeys().arrayListValues().build();
        ListMultimap<String, String> definedMetadataSeparators = ListMultimapBuilder.hashKeys().arrayListValues().build();

        MultimapToSingleStringMapWrapper map = new MultimapToSingleStringMapWrapper(underlyingMetadata, underlyingMetadataSeparators, definedMetadataSeparators);
        
        Assertions.assertNull(map.get("foo"));
    }

    @Test
    void testGet() {
        ListMultimap<String, String> underlyingMetadata = ListMultimapBuilder.hashKeys().arrayListValues().build();
        underlyingMetadata.putAll("foo", Lists.newArrayList("a","b","c","d"));

        ListMultimap<String, String> underlyingMetadataSeparators = ListMultimapBuilder.hashKeys().arrayListValues().build();
        underlyingMetadataSeparators.putAll("foo", Lists.newArrayList("|",";"));

        ListMultimap<String, String> definedMetadataSeparators = ListMultimapBuilder.hashKeys().arrayListValues().build();
        definedMetadataSeparators.putAll("foo", Lists.newArrayList(":","+"));

        MultimapToSingleStringMapWrapper map = new MultimapToSingleStringMapWrapper(underlyingMetadata, underlyingMetadataSeparators, definedMetadataSeparators);
        
        Assertions.assertEquals("a|b;c:d", map.get("foo"));
    }

    @Test
    void testPut() {
        ListMultimap<String, String> underlyingMetadata = ListMultimapBuilder.hashKeys().arrayListValues().build();
        ListMultimap<String, String> underlyingMetadataSeparators = ListMultimapBuilder.hashKeys().arrayListValues().build();
        ListMultimap<String, String> definedMetadataSeparators = ListMultimapBuilder.hashKeys().arrayListValues().build();
        definedMetadataSeparators.putAll("foo", Lists.newArrayList("|","+"));

        MultimapToSingleStringMapWrapper map = new MultimapToSingleStringMapWrapper(underlyingMetadata, underlyingMetadataSeparators, definedMetadataSeparators);

        Assertions.assertNull(map.get("foo"));

        map.put("foo", "bar+goo|gar");
        
        Assertions.assertEquals("bar+goo|gar", map.get("foo"));
        Assertions.assertEquals(Lists.newArrayList("bar","goo","gar"), underlyingMetadata.get("foo"));
        Assertions.assertEquals(Lists.newArrayList("+","|"), underlyingMetadataSeparators.get("foo"));
    }
}