package com.funnelback.publicui.test.contentauditor;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.funnelback.publicui.contentauditor.UrlScopeFill;

@RunWith(Parameterized.class)
public class UrlScopeFillSegmentCountTests {

    @Parameters(name = "{index}: countSegments({0}) => {1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] { 
            {"example.com/path", 3}
            ,{"example.com/path/", 3}
            ,{"example.com/", 2}
            ,{"example.com", 2}
            ,{"com/", 1}
            ,{"com", 1}
            ,{"", 0}
           });
    }
    
    private String input;
    private int expectedCount;

    public UrlScopeFillSegmentCountTests(String input, int expectedCount) {
        this.input = input;
        this.expectedCount = expectedCount;
    }

    @Test
    public void test() {
        int count = UrlScopeFill.countSegments(input);
        Assert.assertEquals("Expected correct count", this.expectedCount, count );
    }
    
}
