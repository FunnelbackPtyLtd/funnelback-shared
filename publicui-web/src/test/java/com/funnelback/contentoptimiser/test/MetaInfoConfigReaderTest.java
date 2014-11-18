package com.funnelback.contentoptimiser.test;

import java.util.Map;

import org.junit.Assert;

import org.junit.Test;

import com.funnelback.contentoptimiser.ConfigReader;
import com.funnelback.contentoptimiser.fetchers.impl.MetaInfoConfigReader;
import com.funnelback.contentoptimiser.processors.impl.MetaInfo;

public class MetaInfoConfigReaderTest {

    @Test public void testWithNoFile() {
        ConfigReader<MetaInfo> reader = new MetaInfoConfigReader();
        Map<String, ? extends MetaInfo> m = reader.read("thisfiledoesntexist");
        Assert.assertEquals(0, m.size());
    }
    
    @Test public void testWithAFile() {
        ConfigReader<MetaInfo> reader = new MetaInfoConfigReader();
        Map<String, ? extends MetaInfo> m = reader.read("src/test/resources/dummy-search_home/conf/meta-names.xml.default");
        Assert.assertEquals(20, m.size());
        Assert.assertEquals("description",m.get("c").getShortTitle());
        Assert.assertEquals("description and summary fields",m.get("c").getLongTitle());
        Assert.assertEquals("Try adding more occurrences of the query term in any summary or description meta tags.",m.get("c").getImprovementSuggestion());
        Assert.assertEquals(new Integer(4),m.get("c").getThreshold());
    }
}
