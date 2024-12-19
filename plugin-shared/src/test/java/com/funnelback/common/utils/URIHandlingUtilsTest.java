package com.funnelback.common.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class URIHandlingUtilsTest {

    @Test
    void testCreateURI() {

        Assertions.assertNull(URIHandlingUtils.create(null));

        Assertions.assertNull(URIHandlingUtils.create(""));

        Assertions.assertNull(URIHandlingUtils.create(" "));

        Assertions.assertEquals("http://www.example.com", URIHandlingUtils.create("http://www.example.com").toString());

        Assertions.assertEquals("www.example.com", URIHandlingUtils.create("www.example.com").toString());

        Assertions.assertEquals("http://www.example.com:8080", URIHandlingUtils.create("http://www.example.com:8080").toString());

        Assertions.assertEquals("http://admin:pass@example.com:99/docUrl#test", URIHandlingUtils.create("http://admin:pass@example.com:99/docUrl#test").toString());

        // It will always encode the "|" but not the other special characters accepted in URI, such as "&",  "?", ";", "/", ":" or "#" if the target string is an absolute URL
        Assertions.assertEquals("http://admin:pass@example.com:99/test?;docUrl=http://admin:pass@example.com#test%7C&1", URIHandlingUtils.create("http://admin:pass@example.com:99/test?;docUrl=http://admin:pass@example.com#test|&1").toString());

        // It will encode all the special characters if the target string  is not an absolute URL
        Assertions.assertEquals("test%3F%3BdocUrl%3Dhttp%3A%2F%2Fexample.com%23test%7C%261", URIHandlingUtils.create("test?;docUrl=http://example.com#test|&1").toString());

    }
}