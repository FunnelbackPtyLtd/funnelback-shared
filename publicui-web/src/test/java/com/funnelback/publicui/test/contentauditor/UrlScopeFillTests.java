package com.funnelback.publicui.test.contentauditor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.funnelback.publicui.contentauditor.UrlScopeFill;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class UrlScopeFillTests {

    @Test
    public void testSimpleCase() {
        Map<String, Integer> countsFromPadre = new HashMap<String, Integer>();
        countsFromPadre.put("example.com/foo", 1);
        countsFromPadre.put("example.com/bar", 1);

        SearchTransaction st = mock(SearchTransaction.class, Mockito.RETURNS_DEEP_STUBS);
        when(st.getResponse().getResultPacket().getUrlCounts()).thenReturn(countsFromPadre);

        List<CategoryValue> categoryValues = new UrlScopeFill("example.com").computeValues(st);

        Map<String, Integer> result = categoryValues.stream().collect(Collectors.toMap(CategoryValue::getData, CategoryValue::getCount));

        Assert.assertEquals(new Integer(1), result.get("example.com/foo/"));
        Assert.assertEquals(new Integer(1), result.get("example.com/bar/"));
    }

    @Test
    public void testLevelSkipping() {
        Map<String, Integer> countsFromPadre = new HashMap<String, Integer>();
        countsFromPadre.put("example.com/same/foo", 1);
        countsFromPadre.put("example.com/same/bar", 1);

        SearchTransaction st = mock(SearchTransaction.class, Mockito.RETURNS_DEEP_STUBS);
        when(st.getResponse().getResultPacket().getUrlCounts()).thenReturn(countsFromPadre);

        List<CategoryValue> categoryValues = new UrlScopeFill("example.com").computeValues(st);

        Map<String, Integer> result = categoryValues.stream().collect(Collectors.toMap(CategoryValue::getData, CategoryValue::getCount));

        // Should go down two levels automatically, because the next level is the same
        Assert.assertEquals(new Integer(1), result.get("example.com/same/foo/"));
        Assert.assertEquals(new Integer(1), result.get("example.com/same/bar/"));
    }

    @Test
    public void testDomainDrill() {
        Map<String, Integer> countsFromPadre = new HashMap<String, Integer>();
        countsFromPadre.put("something.www.example.com", 1);
        countsFromPadre.put("something.www2.example.com", 1);

        SearchTransaction st = mock(SearchTransaction.class, Mockito.RETURNS_DEEP_STUBS);
        when(st.getResponse().getResultPacket().getUrlCounts()).thenReturn(countsFromPadre);

        List<CategoryValue> categoryValues = new UrlScopeFill("").computeValues(st);

        Map<String, Integer> result = categoryValues.stream().collect(Collectors.toMap(CategoryValue::getData, CategoryValue::getCount));

        // Should drill down in the URL until unique
        Assert.assertEquals(new Integer(1), result.get("www.example.com/"));
        Assert.assertEquals(new Integer(1), result.get("www2.example.com/"));
    }

}
