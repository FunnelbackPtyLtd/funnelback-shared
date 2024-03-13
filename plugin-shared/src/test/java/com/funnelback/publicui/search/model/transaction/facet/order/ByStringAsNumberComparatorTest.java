package com.funnelback.publicui.search.model.transaction.facet.order;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ByStringAsNumberComparatorTest {

    private final ByStringAsNumberComparator comparator = new ByStringAsNumberComparator();
    
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
        Assertions.assertNull(comparator.extractFirstNumber(""));
        Assertions.assertNull(comparator.extractFirstNumber("-"));
        Assertions.assertNull(comparator.extractFirstNumber("."));
        Assertions.assertNull(comparator.extractFirstNumber("sd.sd"));
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
        
        list.sort(comparator);

        Assertions.assertEquals("-1.1", list.get(0));
        Assertions.assertEquals("-1", list.get(1));
        Assertions.assertEquals("0", list.get(2));
        Assertions.assertEquals("1", list.get(3));
        Assertions.assertEquals("1.4", list.get(4));
        Assertions.assertEquals("4", list.get(5));
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
        
        list.sort(comparator);

        Assertions.assertEquals("$-1.1--1", list.get(0));
        Assertions.assertEquals("$-1-0", list.get(1));
        Assertions.assertEquals("$0-1", list.get(2));
        Assertions.assertEquals("$1-4", list.get(3));
        Assertions.assertEquals("$1.4-4", list.get(4));
        Assertions.assertEquals("$4-100", list.get(5));
        Assertions.assertEquals("ALL", list.get(6));
    }
    
    private void testInt(int expected, String str) {
        Assertions.assertEquals(expected, comparator.extractFirstNumber(str).intValue(), "Expected: " + expected + "for '" + str + "'");
    }
    
    private void testExtract(String expected, String str) {
        Assertions.assertEquals(expected, comparator.extractFirstNumber(str).toString(), "Expected: " + expected + " for '" + str + "'");
    }
}