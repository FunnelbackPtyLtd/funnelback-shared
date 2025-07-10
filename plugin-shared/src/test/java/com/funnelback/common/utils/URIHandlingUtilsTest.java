package com.funnelback.common.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class URIHandlingUtilsTest {

    @ParameterizedTest
    @CsvSource({
        "http://www.example.com, http://www.example.com",
        "   http://www.example.com, http://www.example.com",
        "http://example.com/path?param=value, http://example.com/path?param=value",
        "https://example.com:8080/path, https://example.com:8080/path",
        "https://example.com/path with spaces/and+plus&symbols=value, https://example.com/path%20with%20spaces/and+plus&symbols=value",
        "https://example.com/path/with/unicode/测试, https://example.com/path/with/unicode/测试",
        "https://example.com/path#fragment, https://example.com/path#fragment",
        "http://example.com:8080/path, http://example.com:8080/path",
        "https://user:pass@example.com/path, https://user:pass@example.com/path",
        "http://admin:pass@example.com:99/docUrl#test, http://admin:pass@example.com:99/docUrl#test",
        // It will always encode the "|" but not the other special characters accepted in URI, such as "&",  "?", ";", "/", ":" or "#" if the target string is an absolute URL
        "http://admin:pass@example.com:99/test?;docUrl=http://admin:pass@example.com#test|&1, http://admin:pass@example.com:99/test?;docUrl=http://admin:pass@example.com#test%7C&1",
        "http://169.254.169.255/metadata, http://169.254.169.255/metadata",
        "http://192.168.1.1/metadata, http://192.168.1.1/metadata",
        // It will encode all the special characters if the target string is not an absolute URL
        "test?;docUrl=http://example.com#test|&1, test%3F%3BdocUrl%3Dhttp%3A%2F%2Fexample.com%23test%7C%261",
        "not-a-url, not-a-url",
        "http://, http%3A%2F%2F",
        "https://, https%3A%2F%2F",
        "://example.com, %3A%2F%2Fexample.com",
        "http://example.com:invalid-port, http%3A%2F%2Fexample.com%3Ainvalid-port",
        "169.254.169.254, 169.254.169.254"
    })
    void testValidUrl(String inputUrl, String expectedUrl) {
        assertEquals(expectedUrl, URIHandlingUtils.create(inputUrl).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "   " })
    @NullSource
    @EmptySource
    void testInvalidUrlWithNull(String invalidUrl) {
        assertThrows(NullPointerException.class, () -> URIHandlingUtils.create(invalidUrl).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "http://169.254.169.254",
        "http://169.254.169.254/metadata",
        "http://169.254.169.254:8080/metadata",
        "https://169.254.169.254/metadata",
        "http://user@169.254.169.254/metadata"
    })
    void testInvalidUrlWithIpAddress(String invalidUrl) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> URIHandlingUtils.create(invalidUrl).toString());

        assertEquals("Access to cloud provider metadata service is not allowed", exception.getMessage());
    }
}