package com.funnelback.publicui.search.model.transaction.facet.order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;


public class ByStringAsNumberComparatorTest {

    private ByStringAsNumberComparator comparator = new ByStringAsNumberComparator();
    
    @Test
    public void testNumberExtraction() {
        testInt(1, "١.٠");
        testInt(1, "1");
        testInt(1, "1  1");
        testInt(11, "1 1");
        testInt(1, "1, 1");
        testInt(-1, "-1");
        testInt(0, "0");
        testInt(0, "0.0");
        testInt(-1, "asas-1-123");
        testInt(0, "0 ");
        testInt(111, "111");
        
    }
    
    @Test
    public void testDecimal() {
        testExtract("0.12", "0.12");
        testExtract("-0.12", "-0.12");
        // https://en.wikipedia.org/wiki/ISO_31-0#Numbers
        // For numbers whose magnitude is less than 1, the decimal sign should be preceded by a zero.
        testExtract("12", "-.12");
        testExtract("-0.1", "as-0.1-2");
        testExtract("1.2", "1.2.3");
        testExtract("100000000.12", "100\u200A000\u200A000,12");
    }
    
    @Test
    public void testNoNumber() {
        Assert.assertNull(comparator.extractFirstNumber(""));
        Assert.assertNull(comparator.extractFirstNumber("-"));
        Assert.assertNull(comparator.extractFirstNumber("."));
        Assert.assertNull(comparator.extractFirstNumber("sd.sd"));
    }
    
    @Test
    public void testSortNumbers() {
        List<String> list = new ArrayList<>();
        list.add(("4"));
        list.add(("1.4"));
        list.add(("1"));
        list.add(("0"));
        list.add(("-1.1"));
        list.add(("-1"));
        
        Collections.sort(list, comparator);
        
        List<String> result = list;
        
        Assert.assertEquals("-1.1", result.get(0));
        Assert.assertEquals("-1", result.get(1));
        Assert.assertEquals("0", result.get(2));
        Assert.assertEquals("1", result.get(3));
        Assert.assertEquals("1.4", result.get(4));
        Assert.assertEquals("4", result.get(5));
    }
    
    @Test
    public void testSortRanges() {
        List<String> list = new ArrayList<>();
        list.add(("ALL"));
        list.add(("$4-100"));
        list.add(("$1.4-4"));
        list.add(("$1-4"));
        list.add(("$0-1"));
        list.add(("$-1.1--1"));
        list.add(("$-1-0"));
        
        Collections.sort(list, comparator);
        
        List<String> result = list;
        
        Assert.assertEquals("$-1.1--1", result.get(0));
        Assert.assertEquals("$-1-0", result.get(1));
        Assert.assertEquals("$0-1", result.get(2));
        Assert.assertEquals("$1-4", result.get(3));
        Assert.assertEquals("$1.4-4", result.get(4));
        Assert.assertEquals("$4-100", result.get(5));
        Assert.assertEquals("ALL", result.get(6));
    }
    
    
    private void testInt(int expected, String str) {
        Assert.assertEquals("Expected: " + expected + "for '" + str + "'",
            expected, comparator.extractFirstNumber(str).intValue());
    }
    
    private void testExtract(String expected, String str) {
        Assert.assertEquals("Expected: " + expected + " for '" + str + "'",
            expected, comparator.extractFirstNumber(str).toString());
    }
}
