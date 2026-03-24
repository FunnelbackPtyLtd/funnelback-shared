package com.funnelback.publicui.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class URLSignatureTest {

    @Test
    void testDifferentParams() {
        assertThat(URLSignature.computeQueryStringSignature("param1=value1&param2=value2"))
            .isNotSameAs(URLSignature.computeQueryStringSignature("param1=value1&param2=value2"));
    }

    @Test
    void testIdenticalParams() {
        assertThat(URLSignature.computeQueryStringSignature("param1=value1&param2=value2"))
            .isEqualTo(URLSignature.computeQueryStringSignature("param1=value1&param2=value2"));
    }

    @Test
    void testIdenticalParamsQuestionMark() {
        assertThat(URLSignature.computeQueryStringSignature("?param1=value1&param2=value2"))
            .isEqualTo(URLSignature.computeQueryStringSignature("param1=value1&param2=value2"));
    }

    @Test
    void testOrder() {
        assertThat(URLSignature.computeQueryStringSignature("param1=value1&param2=value2"))
            .isEqualTo(URLSignature.computeQueryStringSignature("param2=value2&param1=value1"));
    }

    @Test
    void testEncoding() {
        assertThat(URLSignature.computeQueryStringSignature("param+1=value+1&param+2=value+2"))
            .isEqualTo(URLSignature.computeQueryStringSignature("param+2=value+2&param+1=value+1"));
        assertThat(URLSignature.computeQueryStringSignature("param+1=value%201&param%202=value+2"))
            .isEqualTo(URLSignature.computeQueryStringSignature("param%202=value+2&param+1=value%201"));
    }

    @Test
    void testMultipleValues() {
        assertThat(URLSignature.computeQueryStringSignature("param%201=value+1&param+2=value+2&param%202=value+3"))
            .isEqualTo(URLSignature.computeQueryStringSignature("param+2=value%202&param+1=value+1&param+2=value%203"));
        assertThat(URLSignature.computeQueryStringSignature("param%201=value+1&param+2=value+2&param%202=value+3"))
            .isNotSameAs(URLSignature.computeQueryStringSignature("param+2=value%202&param+1=value+1"));
    }

    @Test
    void testNoValue() {
        assertThat(URLSignature.computeQueryStringSignature("param1=value1&param2=value2&param3="))
            .isEqualTo(URLSignature.computeQueryStringSignature("param3=&param1=value1&param2=value2"));
    }

    @Test
    void testStringMadeForHashing() {
        String s = URLSignature.canonicaliseQueryStringToBeHashed("p=foo%2525%25");
        assertThat(s).isEqualTo("pfoo%25%");
    }

    @Test
    void testNull() {
        assertThatThrownBy(() -> URLSignature.computeQueryStringSignature(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testEmptyString() {
        int signature1 = URLSignature.computeQueryStringSignature("");
        int signature2 = URLSignature.computeQueryStringSignature("?");
        assertThat(signature2).isEqualTo(signature1);
    }

    @Test
    void testSpecialCharacters() {
        String query1 = "param=value&special=%21%40%23%24%25%5E%26%2A";
        String query2 = "special=%21%40%23%24%25%5E%26%2A&param=value";
        assertThat(URLSignature.computeQueryStringSignature(query2))
            .isEqualTo(URLSignature.computeQueryStringSignature(query1));
    }

    @Test
    void testUnicodeCharacters() {
        String query1 = "param=测试&value=value";
        String query2 = "value=value&param=测试";
        assertThat(URLSignature.computeQueryStringSignature(query2))
            .isEqualTo(URLSignature.computeQueryStringSignature(query1));
    }
}
