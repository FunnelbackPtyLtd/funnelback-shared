package com.funnelback.publicui.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class URLSignatureTest {

    @Test
    void testDifferentParams() {
        Assertions.assertNotSame(URLSignature.computeQueryStringSignature("param1=value1&param2=value2"), URLSignature.computeQueryStringSignature("param1=value1&param2=value2"));
    }

    @Test
    void testIdenticalParams() {
        Assertions.assertEquals(URLSignature.computeQueryStringSignature("param1=value1&param2=value2"), URLSignature.computeQueryStringSignature("param1=value1&param2=value2"));
    }

    @Test
    void testIdenticalParamsQuestionMark() {
        Assertions.assertEquals(URLSignature.computeQueryStringSignature("?param1=value1&param2=value2"), URLSignature.computeQueryStringSignature("param1=value1&param2=value2"));
    }

    @Test
    void testOrder() {
        Assertions.assertEquals(URLSignature.computeQueryStringSignature("param1=value1&param2=value2"), URLSignature.computeQueryStringSignature("param2=value2&param1=value1"));
    }

    @Test
    void testEncoding() {
        Assertions.assertEquals(URLSignature.computeQueryStringSignature("param+1=value+1&param+2=value+2"), URLSignature.computeQueryStringSignature("param+2=value+2&param+1=value+1"));
        Assertions.assertEquals(URLSignature.computeQueryStringSignature("param+1=value%201&param%202=value+2"), URLSignature.computeQueryStringSignature("param%202=value+2&param+1=value%201"));
    }
    
    @Test
    void testMultipleValues() {
        Assertions.assertEquals(URLSignature.computeQueryStringSignature("param%201=value+1&param+2=value+2&param%202=value+3"), URLSignature.computeQueryStringSignature("param+2=value%202&param+1=value+1&param+2=value%203"));
        Assertions.assertNotSame(URLSignature.computeQueryStringSignature("param%201=value+1&param+2=value+2&param%202=value+3"), URLSignature.computeQueryStringSignature("param+2=value%202&param+1=value+1"));
    }
    
    @Test
    void testNoValue() {
        Assertions.assertEquals(URLSignature.computeQueryStringSignature("param1=value1&param2=value2&param3="), URLSignature.computeQueryStringSignature("param3=&param1=value1&param2=value2"));
    }
    
    @Test
    void testStringMadeForHashing() {
        String s = URLSignature.canonicaliseQueryStringToBeHashed("p=foo%2525%25");
        Assertions.assertEquals("pfoo%25%", s);
    }
    
    @Test
    void testNull() {
        Assertions.assertThrows(NullPointerException.class, () -> URLSignature.computeQueryStringSignature(null));
    }
}