package com.funnelback.publicui.test.utils;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.utils.MultimapToSingleStringMapWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MultimapToSingleStringMapWrapperTest {

    @Test
    public void testEmpty() {
        Map<String, List<String>> underlyingMetadata = Maps.newHashMap();

        Map<String, List<String>> underlyingMetadataSeparators = Maps.newHashMap();

        Map<String, List<String>> definedMetadataSeparators = Maps.newHashMap();

        MultimapToSingleStringMapWrapper map = new MultimapToSingleStringMapWrapper(underlyingMetadata, underlyingMetadataSeparators, definedMetadataSeparators);
        
        Assert.assertNull(map.get("foo"));
    }

    @Test
    public void testGet() {
        Map<String, List<String>> underlyingMetadata = Maps.newHashMap();
        underlyingMetadata.put("foo", Lists.newArrayList("a","b","c","d"));

        Map<String, List<String>> underlyingMetadataSeparators = Maps.newHashMap();
        underlyingMetadataSeparators.put("foo", Lists.newArrayList("|",";"));

        Map<String, List<String>> definedMetadataSeparators = Maps.newHashMap();
        definedMetadataSeparators.put("foo", Lists.newArrayList(":","+"));

        MultimapToSingleStringMapWrapper map = new MultimapToSingleStringMapWrapper(underlyingMetadata, underlyingMetadataSeparators, definedMetadataSeparators);
        
        Assert.assertEquals("a|b;c:d", map.get("foo"));
    }

    @Test
    public void testPut() {
        Map<String, List<String>> underlyingMetadata = Maps.newHashMap();

        Map<String, List<String>> underlyingMetadataSeparators = Maps.newHashMap();

        Map<String, List<String>> definedMetadataSeparators = Maps.newHashMap();
        definedMetadataSeparators.put("foo", Lists.newArrayList("|","+"));

        MultimapToSingleStringMapWrapper map = new MultimapToSingleStringMapWrapper(underlyingMetadata, underlyingMetadataSeparators, definedMetadataSeparators);

        Assert.assertEquals(null, map.get("foo"));

        map.put("foo", "bar+goo|gar");
        
        Assert.assertEquals("bar+goo|gar", map.get("foo"));
        Assert.assertEquals(Lists.newArrayList("bar","goo","gar"), underlyingMetadata.get("foo"));
        Assert.assertEquals(Lists.newArrayList("+","|"), underlyingMetadataSeparators.get("foo"));
    }

}
