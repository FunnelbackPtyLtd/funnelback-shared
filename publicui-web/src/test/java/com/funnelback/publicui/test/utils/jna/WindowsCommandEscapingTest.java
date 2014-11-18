package com.funnelback.publicui.test.utils.jna;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.utils.jna.WindowsCommandEscaping;

public class WindowsCommandEscapingTest {
    
    private static final String[][] testStringPairs = new String[][] {
        {"",
        "\"\""},

        {"argument1",
        "\"argument1\""},

        {"argument 2",
        "\"argument 2\""},

        {"she said, \"you had me at hello\"",
        "\"she said, \\\"you had me at hello\\\"\""},

        {"\\some\\path with\\spaces",
        "\"\\some\\path with\\spaces\""},

        {"I want to use \"|,>,<,\\\", as well as \"&,?,% and ^\"",
        "\"I want to use \\\"|,>,<,\\\\\\\", as well as \\\"&,?,% and ^\\\"\""},

        {"-userkeys=sport\\DomainUsers,sitecore\\ExternalUser",
        "\"-userkeys=sport\\DomainUsers,sitecore\\ExternalUser\""},
        
        {
            "-userkeys=Se2-PublicUI-Meta-Trim-Component;Se2-PublicUI-Meta-Trim-Component:Unclassified:level,Se2-PublicUI-Meta-Trim-Component;Se2-PublicUI-Meta-Trim-Component:Top Secret:level,Se2-PublicUI-Meta-Trim-Component;Se2-PublicUI-Meta-Trim-Component:Secret:level,Se2-PublicUI-Meta-Trim-Component;Se2-PublicUI-Meta-Trim-Component:Confidential:level,Se2-PublicUI-Meta-Trim-Component;Se2-PublicUI-Meta-Trim-Component:Staff in Confidence:caveat,Se2-PublicUI-Meta-Trim-Component;Se2-PublicUI-Meta-Trim-Component:Medical in Confidence:caveat,Se2-PublicUI-Meta-Trim-Component;Se2-PublicUI-Meta-Trim-Component:Cabinet in Confidence:caveat,Se2-PublicUI-Meta-Trim-Component;Se2-PublicUI-Meta-Trim-Component:Trusted Commercial:caveat,Se2-PublicUI-Meta-Trim-Component;Se2-PublicUI-Meta-Trim-Component:Research Projects:caveat,Se2-PublicUI-Meta-Trim-Component;Se2-PublicUI-Meta-Trim-Component:Everyone:acl,Se2-PublicUI-Meta-Trim-Component;Se2-PublicUI-Meta-Trim-Component:Administrator:acl",
            "\"-userkeys=Se2-PublicUI-Meta-Trim-Component;Se2-PublicUI-Meta-Trim-Component:Unclassified:level,Se2-PublicUI-Meta-Trim-Component;Se2-PublicUI-Meta-Trim-Component:Top Secret:level,Se2-PublicUI-Meta-Trim-Component;Se2-PublicUI-Meta-Trim-Component:Secret:level,Se2-PublicUI-Meta-Trim-Component;Se2-PublicUI-Meta-Trim-Component:Confidential:level,Se2-PublicUI-Meta-Trim-Component;Se2-PublicUI-Meta-Trim-Component:Staff in Confidence:caveat,Se2-PublicUI-Meta-Trim-Component;Se2-PublicUI-Meta-Trim-Component:Medical in Confidence:caveat,Se2-PublicUI-Meta-Trim-Component;Se2-PublicUI-Meta-Trim-Component:Cabinet in Confidence:caveat,Se2-PublicUI-Meta-Trim-Component;Se2-PublicUI-Meta-Trim-Component:Trusted Commercial:caveat,Se2-PublicUI-Meta-Trim-Component;Se2-PublicUI-Meta-Trim-Component:Research Projects:caveat,Se2-PublicUI-Meta-Trim-Component;Se2-PublicUI-Meta-Trim-Component:Everyone:acl,Se2-PublicUI-Meta-Trim-Component;Se2-PublicUI-Meta-Trim-Component:Administrator:acl\""
        }
        

    };

    @Test
    public void testEscapingSingleArgument() {
        for(String[] testPair : testStringPairs) {
            Assert.assertEquals(
                testPair[1],
                WindowsCommandEscaping.argvQuote(testPair[0]));
        }
    }
}
