package com.funnelback.publicui.test.contentauditor;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.contentauditor.UrlScopeFill;

public class SimpleParsedUriSplittingTests {
    @Test
    public void pathPrefixTest() {
        UrlScopeFill.SimpleParsedUri uri = new UrlScopeFill.SimpleParsedUri("http://example.com/one/two/three.html");
        Assert.assertEquals("Expected three path prefixes", 3, uri.getPathPrefixes().size());
        Assert.assertTrue("Expected 'one' in the path prefixes", uri.getPathPrefixes().contains("one"));
        Assert.assertTrue("Expected 'one/two' in the path prefixes", uri.getPathPrefixes().contains("one/two"));
        Assert.assertTrue("Expected 'one/two/three' in the path prefixes", uri.getPathPrefixes().contains("one/two/three.html"));
    }

    @Test
    public void pathPrefixEmptyTest() {
        UrlScopeFill.SimpleParsedUri uri = new UrlScopeFill.SimpleParsedUri("http://example.com/");
        Assert.assertTrue("Expected no path prefixes", uri.getPathPrefixes().size() == 0);
        UrlScopeFill.SimpleParsedUri uri2 = new UrlScopeFill.SimpleParsedUri("http://example.com");
        Assert.assertTrue("Expected no path prefixes", uri2.getPathPrefixes().size() == 0);
    }

    @Test
    public void hostnameSuffixTest() {
        UrlScopeFill.SimpleParsedUri uri = new UrlScopeFill.SimpleParsedUri("http://www.business.qld.gov.au/example.html");
        Assert.assertEquals("Expected five hostname suffixes", 5, uri.getHostnameSuffixes().size());
        Assert.assertTrue("Expected 'au' in the hostname suffixes", uri.getHostnameSuffixes().contains("au"));
        Assert.assertTrue("Expected 'gov.au' in the hostname suffixes", uri.getHostnameSuffixes().contains("gov.au"));
        Assert.assertTrue("Expected 'qld.gov.au' in the hostname suffixes", uri.getHostnameSuffixes().contains("qld.gov.au"));
        Assert.assertTrue("Expected 'business.qld.gov.au' in the hostname suffixes", uri.getHostnameSuffixes().contains("business.qld.gov.au"));
        Assert.assertTrue("Expected 'www.business.qld.gov.au' in the hostname suffixes", uri.getHostnameSuffixes().contains("www.business.qld.gov.au"));
    }

    @Test
    public void intranetHostnameSuffixTest() {
        UrlScopeFill.SimpleParsedUri uri = new UrlScopeFill.SimpleParsedUri("http://intranet/example.html");
        Assert.assertEquals("Expected one hostname suffixe", 1, uri.getHostnameSuffixes().size());
        Assert.assertTrue("Expected 'intranet' in the hostname suffixes", uri.getHostnameSuffixes().contains("intranet"));
    }
}
