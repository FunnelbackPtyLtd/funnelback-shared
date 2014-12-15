package com.funnelback.publicui.test.contentauditor;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized;

import com.funnelback.publicui.contentauditor.UrlScopeFill;

@RunWith(Parameterized.class)
public class SimpleParsedUriTests {

    @Parameters(name = "{index}: parse({0}) => {1},{2},{3}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] { 
            {"https://example.com/path", "https", "example.com", "path"}
            ,{"https://example.com/", "https", "example.com", ""}
            ,{"https://example.com", "https", "example.com", ""}
            ,{"https://example.com:8443", "https", "example.com:8443", ""}
            ,{"example.com", "http", "example.com", ""}
            ,{"example", "http", "example", ""}
            ,{"smb://example", "smb", "example", ""}
            ,{"example/path/with/several/levels.docx", "http", "example", "path/with/several/levels.docx"}
           });
    }
    
    private String input;
    private String expectedProtocol;
    private String expectedHostname;
    private String expectedPath;

    public SimpleParsedUriTests(String input, String expectedProtocol, String expectedHostname, String expectedPath) {
        this.input = input;
        this.expectedProtocol = expectedProtocol;
        this.expectedHostname = expectedHostname;
        this.expectedPath = expectedPath;
    }
    
    @Test
    public void parsingTest() {
        UrlScopeFill.SimpleParsedUri uri = new UrlScopeFill.SimpleParsedUri(input);
        Assert.assertEquals("Expected correct protocol", this.expectedProtocol, uri.getProtocol() );
        Assert.assertEquals("Expected correct hostname", this.expectedHostname, uri.getHostname() );
        Assert.assertEquals("Expected correct path", this.expectedPath, uri.getPath() );
    }

}
