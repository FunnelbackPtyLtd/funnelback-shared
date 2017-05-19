package com.funnelback.publicui.streamedresults;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.streamedresults.datafetcher.XJPathResultDataFetcher;



public class ResultFieldsTest {

    @Test
    public void testBothListsMissing() {
        ResultFields resultFields = new ResultFields(Optional.empty(), Optional.empty());
        Assert.assertEquals("liveUrl", resultFields.getXPaths().get(0));
        Assert.assertEquals("Live URL", resultFields.getFieldNames().get(0));
        
        Result result = new Result();
        result.setLiveUrl("live");
        XJPathResultDataFetcher resultFetcher = new XJPathResultDataFetcher();
        List<Object> values = resultFetcher.fetchFeilds(resultFields.getFieldNames(), resultFetcher.parseFields(resultFields.getXPaths()), result);
        
        Assert.assertEquals("Perhaps the default XPath is no longer valid", "live", values.get(0).toString());
    }
    
    @Test
    public void testMissingXPaths() {
        ResultFields resultFields = new ResultFields(Optional.empty(), Optional.of(Arrays.asList("Name A")));
        Assert.assertEquals(1, resultFields.getXPaths().size());
        Assert.assertEquals("Name A", resultFields.getFieldNames().get(0));
        
        Result result = new Result();
        result.setLiveUrl("live");
        XJPathResultDataFetcher resultFetcher = new XJPathResultDataFetcher();
        List<Object> values = resultFetcher.fetchFeilds(resultFields.getFieldNames(), resultFetcher.parseFields(resultFields.getXPaths()), result);
        
        Assert.assertEquals(1, values.size());
        Assert.assertNull(values.get(0));
    }
    
    @Test
    public void testMissingFieldNames() {
        ResultFields resultFields = new ResultFields(Optional.of(Arrays.asList("xpath")), Optional.empty());
        Assert.assertEquals("xpath", resultFields.getXPaths().get(0));
        
        Assert.assertEquals(1, resultFields.getFieldNames().size());
        Assert.assertEquals("xpath", resultFields.getFieldNames().get(0));
    }
    
    @Test
    public void testEnoughOfBoth() {
        ResultFields resultFields = new ResultFields(Optional.of(Arrays.asList("xpath")), Optional.of(Arrays.asList("My XPath")));
        Assert.assertEquals("xpath", resultFields.getXPaths().get(0));
        
        Assert.assertEquals("My XPath", resultFields.getFieldNames().get(0));
    }
}
