package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.FacetScope;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.QueryStringUtils;

import lombok.SneakyThrows;

public class FacetScopeTests {

    private SearchTransaction st;
    private FacetScope processor;
    
    @Before
    public void before() {
        processor = new FacetScope();
        st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
    }
    
    @Test
    public void testInvalidParameters() {
        try {
            processor.processInput(null);
            processor.processInput(new SearchTransaction(null, null));
            processor.processInput(new SearchTransaction(new SearchQuestion(), null));
        } catch (Exception e) {
            Assert.fail();
        }
    }
    
    @Test
    public void testNoFacetScope() throws InputProcessorException {
        Assert.assertEquals(0, st.getQuestion().getRawInputParameters().size());
        
        processor.processInput(st);

        Assert.assertEquals(0, st.getQuestion().getRawInputParameters().size());
    }
    
    @Test
    public void testNoValue() throws InputProcessorException {
        st.getQuestion().getRawInputParameters().put("facetScope", new String[0]);
        processor.processInput(st);
        Assert.assertEquals(1, st.getQuestion().getRawInputParameters().size());
        Assert.assertTrue(st.getQuestion().getRawInputParameters().containsKey("facetScope"));
        Assert.assertEquals(0, st.getQuestion().getRawInputParameters().get("facetScope").length);
    }

    @Test
    public void testEmptyValue() throws InputProcessorException {
        st.getQuestion().getRawInputParameters().put("facetScope", new String[] {""});
        processor.processInput(st);
        Assert.assertEquals(1, st.getQuestion().getRawInputParameters().size());
        Assert.assertTrue(st.getQuestion().getRawInputParameters().containsKey("facetScope"));
        Assert.assertEquals(1, st.getQuestion().getRawInputParameters().get("facetScope").length);
        Assert.assertEquals("", st.getQuestion().getRawInputParameters().get("facetScope")[0]);
    }

    @Test
    public void test() throws Exception {
        st.getQuestion().getRawInputParameters().put("facetScope", new String[] {
                //encode(
                        encode("f.Location|X") + "=" + encode("australia")
                        + "&" + encode("f.Type|1") + "=" + encode("part time")
                        + "&" + encode("f.Url|url") + "=" + encode("prospects & sales")    // With ampersand
                        + "&" + encode("f.Type|1") + "=" + encode("full time")});//);            // Second value for same param
        
        processor.processInput(st);

        // 4 because "facetScope" is still in the Map
        Assert.assertEquals(4, st.getQuestion().getRawInputParameters().size());
        
        for (String s: new String[] {"f.Location|X", "f.Type|1", "f.Url|url"}) {
            Assert.assertTrue(st.getQuestion().getRawInputParameters().keySet().contains(s));
        }
        
        Assert.assertEquals(st.getQuestion().getRawInputParameters().get("f.Location|X")[0], "australia");
        Assert.assertEquals(st.getQuestion().getRawInputParameters().get("f.Url|url")[0], "prospects & sales");
        Assert.assertTrue(st.getQuestion().getRawInputParameters().get("f.Type|1")[0].equals("part time")
                || st.getQuestion().getRawInputParameters().get("f.Type|1")[0].equals("full time"));
        

        Assert.assertEquals(st.getQuestion().getRawInputParameters().get("f.Location|X")[0], "australia");
        Assert.assertEquals(st.getQuestion().getRawInputParameters().get("f.Url|url")[0], "prospects & sales");
        
        String[] s = st.getQuestion().getRawInputParameters().get("f.Type|1");
        Assert.assertEquals(2, s.length);
        Assert.assertTrue(s[0].equals("part time") || s[0].equals("full time"));
        Assert.assertTrue(s[1].equals("part time") || s[1].equals("full time"));
    }
    
    @SneakyThrows(UnsupportedEncodingException.class)
    private String encode(String s) {
        return URLEncoder.encode(s, "UTF-8");
    }

    @Test
    public void testConvertFacetScopeUrl() throws MalformedURLException {
        String s = FacetScope.convertFacetScopeParameters(
            "collection=test-coursefinder-mq&form=coursefinder&query=science&facetScope=f.ATAR%2520cut-off%257CA%3D82.30&profile=_default");
        
        Map<String, String> qs = QueryStringUtils.toSingleMap(s);
        Assert.assertEquals(5, qs.size());
        Assert.assertEquals("test-coursefinder-mq", qs.get("collection"));
        Assert.assertEquals("coursefinder", qs.get("form"));
        Assert.assertEquals("science", qs.get("query"));
        Assert.assertEquals("82.30", qs.get("f.ATAR cut-off|A"));
        Assert.assertEquals("_default", qs.get("profile"));
    }
    
    @Test
    public void testOptionInBothFacetScopeAndNonFacetScopeForm() throws Exception {
        st.getQuestion().getRawInputParameters().put("a", new String[]{"foo"});
        st.getQuestion().getRawInputParameters().put("facetScope", new String[] {
                //encode(
                        encode("a") + "=" + encode("b")
                        + "&" + encode("a") + "=" + encode("c")
                        });//);            // Second value for same param
        
        processor.processInput(st);

        
        
        
        Assert.assertTrue(st.getQuestion().getRawInputParameters().keySet().contains("a"));
        
        Set<String> values = Arrays.asList(st.getQuestion().getRawInputParameters().get("a")).stream().collect(Collectors.toSet());
        
        Assert.assertTrue(values.contains("c"));
        Assert.assertTrue(values.contains("b"));
        Assert.assertTrue(values.contains("foo"));
        
    }
    
    
}
