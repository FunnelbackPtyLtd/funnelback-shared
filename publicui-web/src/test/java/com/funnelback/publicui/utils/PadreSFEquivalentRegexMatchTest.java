package com.funnelback.publicui.utils;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class PadreSFEquivalentRegexMatchTest {

    private PadreSFEquivalentRegexMatch matcher = new PadreSFEquivalentRegexMatch();
    
    @Test
    public void testStandardMetadataClasses() {
        Assert.assertTrue(matches("f", "f"));
        Assert.assertTrue(matches("foo", "foo"));
        Assert.assertTrue(matches("Foo", "Foo"));
        Assert.assertFalse(matches("Foo", "foo"));
        Assert.assertFalse(matches("foo", "f"));
        Assert.assertFalse(matches("f", "foo"));
        Assert.assertFalse(matches("oo", "o"));
        Assert.assertFalse(matches("o", "oo"));
    }
    
    @Test
    public void testFunRegexes() {
        Assert.assertTrue(matches("f", "."));
        Assert.assertTrue(matches("f", "f.*"));
        Assert.assertTrue(matches("foo", "f.*"));
        Assert.assertTrue(matches("Foo", "Fo.+"));
        Assert.assertFalse(matches("Foo", "f."));
        Assert.assertFalse(matches("foo", "f..."));
        Assert.assertFalse(matches("f", ".*f.*."));
    }
    
    private boolean matches(String mdClass, String regex) {
        return matcher.filterToMatchingMetadataClassnames(Arrays.asList(mdClass), Arrays.asList(regex))
            .contains(mdClass);
        
    }
}
