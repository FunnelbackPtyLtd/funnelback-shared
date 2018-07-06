package com.funnelback.publicui.streamedresults.datafetcher;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.function.StreamUtils;
import com.funnelback.publicui.search.model.padre.Result;
import com.google.common.collect.Lists;



public class XJPathResultDataFetcherTest {

    XJPathResultDataFetcher dataFetcher = new XJPathResultDataFetcher();
    
    @Test
    public void testAccessFields() {
        Result result = new Result();
        result.setCacheUrl("http://foo");
        result.getMetaData().put("a", "bar");
        List<Object> data = dataFetcher.fetchFieldValues(dataFetcher.parseFields(toList("cacheUrl", "metaData/a")), result);
        
        Assert.assertEquals("http://foo", data.get(0));
        Assert.assertEquals("bar", data.get(1));
    }
    
    @Test
    public void testEntireResult() {
        Result result = new Result();
        result.setCacheUrl("http://foo");
        result.getMetaData().put("a", "bar");
        List<Object> data = dataFetcher.fetchFieldValues(dataFetcher.parseFields(toList("cacheUrl", "metaData/a")), result);
        
        Assert.assertEquals("http://foo", data.get(0));
        Assert.assertEquals("bar", data.get(1));
    }

    @Test
    public void testListMetadata() {
        Result result = new Result();
        result.setCacheUrl("http://foo");
        result.getListMetadata().put("a", "goo");
        result.getListMetadata().put("a", "gar");
        List<Object> data = dataFetcher.fetchFieldValues(dataFetcher.parseFields(toList("cacheUrl", "listMetadata/a")), result);
        
        Assert.assertEquals("http://foo", data.get(0));
        Assert.assertEquals(Lists.newArrayList("goo","gar"), data.get(1));
    }

    private final List<String> toList(String ... items) {
        return StreamUtils.ofNullable(items).collect(Collectors.toList());
    }
}
