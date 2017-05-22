package com.funnelback.publicui.streamedresults.datafetcher;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.function.StreamUtils;
import com.funnelback.publicui.search.model.padre.Result;



public class XJPathTest {

    XJPathResultDataFetcher dataFetcher = new XJPathResultDataFetcher();
    
    @Test
    public void testAccessFields() {
        Result result = new Result();
        result.setCacheUrl("http://foo");
        result.getMetaData().put("a", "bar");
        List<Object> data = dataFetcher.fetchFeilds(dataFetcher.parseFields(toList("cacheUrl", "metaData/a")), result);
        
        Assert.assertEquals("http://foo", data.get(0));
        Assert.assertEquals("bar", data.get(1));
    }
    
    @Test
    public void testEntireResult() {
        Result result = new Result();
        result.setCacheUrl("http://foo");
        result.getMetaData().put("a", "bar");
        List<Object> data = dataFetcher.fetchFeilds(dataFetcher.parseFields(toList("/")), result);
        
        Assert.assertEquals("http://foo", data.get(0));
        Assert.assertEquals("bar", data.get(1));
    }
    
    private final List<String> toList(String ... items) {
        return StreamUtils.ofNullable(items).collect(Collectors.toList());
    }
}
