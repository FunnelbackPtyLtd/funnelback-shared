package com.funnelback.publicui.test.utils;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.utils.URLSignature;

public class URLSignatureTest {

    @Test
    public void testDifferentParams() {
        Assert.assertNotSame(
            URLSignature.computeQueryStringSignature("param1=value1&param2=value2"),
            URLSignature.computeQueryStringSignature("param1=value1&param2=value2"));
    }

    @Test
    public void testIdenticalParams() {
        Assert.assertEquals(
            URLSignature.computeQueryStringSignature("param1=value1&param2=value2"),
            URLSignature.computeQueryStringSignature("param1=value1&param2=value2"));
    }

    @Test
    public void testIdenticalParamsQuestionMark() {
        Assert.assertEquals(
            URLSignature.computeQueryStringSignature("?param1=value1&param2=value2"),
            URLSignature.computeQueryStringSignature("param1=value1&param2=value2"));
    }

    @Test
    public void testOrder() {
        Assert.assertEquals(
            URLSignature.computeQueryStringSignature("param1=value1&param2=value2"),
            URLSignature.computeQueryStringSignature("param2=value2&param1=value1"));
    }

    @Test
    public void testEncoding() {
        Assert.assertEquals(
            URLSignature.computeQueryStringSignature("param+1=value+1&param+2=value+2"),
            URLSignature.computeQueryStringSignature("param+2=value+2&param+1=value+1"));

        Assert.assertEquals(
            URLSignature.computeQueryStringSignature("param+1=value%201&param%202=value+2"),
            URLSignature.computeQueryStringSignature("param%202=value+2&param+1=value%201"));
    }
    
    @Test
    public void testMultipleValues() {
        Assert.assertEquals(
            URLSignature.computeQueryStringSignature("param%201=value+1&param+2=value+2&param%202=value+3"),
            URLSignature.computeQueryStringSignature("param+2=value%202&param+1=value+1&param+2=value%203"));

        Assert.assertNotSame(
            URLSignature.computeQueryStringSignature("param%201=value+1&param+2=value+2&param%202=value+3"),
            URLSignature.computeQueryStringSignature("param+2=value%202&param+1=value+1"));
    }
    
    @Test
    public void testNoValue() {
        Assert.assertEquals(
            URLSignature.computeQueryStringSignature("param1=value1&param2=value2&param3="),
            URLSignature.computeQueryStringSignature("param3=&param1=value1&param2=value2"));        
    }
    
    @Test
    public void testStringMadeForHashing() {
        String s = URLSignature.canonicaliseQueryStringToBeHashed("p=foo%2525%25");
        Assert.assertEquals("pfoo%25%", s);
    }
    
    @Test(expected=NullPointerException.class)
    public void testNull() {
        URLSignature.computeQueryStringSignature(null);
    }

}
