package com.funnelback.publicui.test.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.funnelback.publicui.utils.URLSignature;

public class URLSignatureTest {

    @Test
    public void testDifferentParams() {
        Assertions.assertNotSame(URLSignature.computeQueryStringSignature("param1=value1&param2=value2"), URLSignature.computeQueryStringSignature("param1=value1&param2=value2"));
    }

    @Test
    public void testIdenticalParams() {
        Assertions.assertEquals(URLSignature.computeQueryStringSignature("param1=value1&param2=value2"), URLSignature.computeQueryStringSignature("param1=value1&param2=value2"));
    }

    @Test
    public void testIdenticalParamsQuestionMark() {
        Assertions.assertEquals(URLSignature.computeQueryStringSignature("?param1=value1&param2=value2"), URLSignature.computeQueryStringSignature("param1=value1&param2=value2"));
    }

    @Test
    public void testOrder() {
        Assertions.assertEquals(URLSignature.computeQueryStringSignature("param1=value1&param2=value2"), URLSignature.computeQueryStringSignature("param2=value2&param1=value1"));
    }

    @Test
    public void testEncoding() {
        Assertions.assertEquals(URLSignature.computeQueryStringSignature("param+1=value+1&param+2=value+2"), URLSignature.computeQueryStringSignature("param+2=value+2&param+1=value+1"));
        Assertions.assertEquals(URLSignature.computeQueryStringSignature("param+1=value%201&param%202=value+2"), URLSignature.computeQueryStringSignature("param%202=value+2&param+1=value%201"));
    }
    
    @Test
    public void testMultipleValues() {
        Assertions.assertEquals(URLSignature.computeQueryStringSignature("param%201=value+1&param+2=value+2&param%202=value+3"), URLSignature.computeQueryStringSignature("param+2=value%202&param+1=value+1&param+2=value%203"));
        Assertions.assertNotSame(URLSignature.computeQueryStringSignature("param%201=value+1&param+2=value+2&param%202=value+3"), URLSignature.computeQueryStringSignature("param+2=value%202&param+1=value+1"));
    }
    
    @Test
    public void testNoValue() {
        Assertions.assertEquals(URLSignature.computeQueryStringSignature("param1=value1&param2=value2&param3="), URLSignature.computeQueryStringSignature("param3=&param1=value1&param2=value2"));
    }
    
    @Test
    public void testStringMadeForHashing() {
        String s = URLSignature.canonicaliseQueryStringToBeHashed("p=foo%2525%25");
        Assertions.assertEquals("pfoo%25%", s);
    }
    
    @Test
    public void testNull() {
        Assertions.assertThrows(NullPointerException.class, () -> URLSignature.computeQueryStringSignature(null));
    }

}