package com.funnelback.publicui.test.utils.jna;

import junit.framework.Assert;
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
        "\"I want to use \\\"|,>,<,\\\\\", as well as \\\"&,?,% and ^\\\"\""},

        {"-userkeys=sport\\DomainUsers,sitecore\\ExternalUser",
        "\"-userkeys=sport\\DomainUsers,sitecore\\ExternalUser\""
        }

    };

    @Test
    public void testEscapingSingleArgument() {
        for(String[] testPair : testStringPairs) {
            Assert.assertTrue(
                testPair[1].equals(
                    WindowsCommandEscaping.argvQuote(testPair[0])));
        }
    }
}
