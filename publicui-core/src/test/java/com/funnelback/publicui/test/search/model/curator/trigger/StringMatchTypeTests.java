package com.funnelback.publicui.test.search.model.curator.trigger;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.curator.trigger.FacetSelectionTrigger;
import com.funnelback.publicui.search.model.curator.trigger.StringMatchType;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class StringMatchTypeTests {

    @Test
    public void testExact() {
        Assert.assertTrue("Expected identical strings to match", StringMatchType.EXACT.matches("foo", "foo"));
        Assert.assertFalse("Expected differnt strings not to match", StringMatchType.EXACT.matches("foo", "bar"));
    }
    
    @Test
    public void testCaseInsensitiveExact() {
        Assert.assertTrue("Expected identical strings to match", StringMatchType.CASE_INSENSITIVE_EXACT.matches("foo", "foo"));
        Assert.assertTrue("Expected case-differing strings to match", StringMatchType.CASE_INSENSITIVE_EXACT.matches("foo", "Foo"));
        Assert.assertFalse("Expected differnt strings not to match", StringMatchType.CASE_INSENSITIVE_EXACT.matches("foo", "bar"));        
    }

    @Test
    public void testSubstring() {
        Assert.assertTrue("Expected identical strings to match", StringMatchType.SUBSTRING.matches("foo", "foo"));
        Assert.assertTrue("Expected substring to match", StringMatchType.SUBSTRING.matches("foo", "food"));
        Assert.assertFalse("Expected case differing substring not to match", StringMatchType.SUBSTRING.matches("foo", "Food"));
        Assert.assertFalse("Expected differnt strings not to match", StringMatchType.SUBSTRING.matches("foo", "bar"));        
    }

    @Test
    public void testCaseInsensitiveSubstring() {
        Assert.assertTrue("Expected identical strings to match", StringMatchType.CASE_INSENSITIVE_SUBSTRING.matches("foo", "foo"));
        Assert.assertTrue("Expected substring to match", StringMatchType.CASE_INSENSITIVE_SUBSTRING.matches("foo", "food"));
        Assert.assertTrue("Expected case differing substring to match", StringMatchType.CASE_INSENSITIVE_SUBSTRING.matches("foo", "Food"));
        Assert.assertFalse("Expected differnt strings not to match", StringMatchType.CASE_INSENSITIVE_SUBSTRING.matches("foo", "bar"));        
    }

    @Test
    public void testRegularExpression() {
        Assert.assertTrue("Expected identical strings to match", StringMatchType.REGULAR_EXPRESSION.matches("foo", "foo"));
        Assert.assertFalse("Expected differnt strings not to match", StringMatchType.REGULAR_EXPRESSION.matches("foo", "bar"));
        Assert.assertTrue("Expected regular expression to match when it should", StringMatchType.REGULAR_EXPRESSION.matches("fo+", "foo"));
    }

    @Test
    public void testInvalidRegularExpression() {
        Assert.assertFalse("Invalid regular expression not to match (but not to error)", StringMatchType.REGULAR_EXPRESSION.matches("fo(", "foo"));
    }

    @Test
    public void testAllWordsContained() {
        Assert.assertTrue("Expected identical strings to match", StringMatchType.ALL_WORDS_CONTAINED.matches("foo", "foo"));
        Assert.assertFalse("Expected different strings not to match", StringMatchType.ALL_WORDS_CONTAINED.matches("foo", "bar"));
        Assert.assertTrue("Expected words to match despite order change", StringMatchType.ALL_WORDS_CONTAINED.matches("foo bar", "bar foo"));
        Assert.assertFalse("Expected not to match due to missing word", StringMatchType.ALL_WORDS_CONTAINED.matches("foo bar goo", "bar foo"));
    }

    @Test
    public void testCaseInsensitiveAllWordsContained() {
        Assert.assertTrue("Expected identical strings to match", StringMatchType.CASE_INSENSITIVE_ALL_WORDS_CONTAINED.matches("foo", "foo"));
        Assert.assertFalse("Expected different strings not to match", StringMatchType.CASE_INSENSITIVE_ALL_WORDS_CONTAINED.matches("foo", "bar"));
        Assert.assertTrue("Expected words to match despite order change, even with differing case", StringMatchType.CASE_INSENSITIVE_ALL_WORDS_CONTAINED.matches("foo bar", "bAr fOo"));
        Assert.assertFalse("Expected not to match due to missing word", StringMatchType.CASE_INSENSITIVE_ALL_WORDS_CONTAINED.matches("foo bar goo", "bar foo"));
    }

    @Test
    public void testNumericGreaterThan() {
        Assert.assertFalse("Expected identical numbers not to match", StringMatchType.NUMERIC_GREATER_THAN.matches("0", "0"));
        Assert.assertTrue("Expected greater than to match", StringMatchType.NUMERIC_GREATER_THAN.matches("50", "0.3"));
        Assert.assertFalse("Expected less than not to match", StringMatchType.NUMERIC_GREATER_THAN.matches("0.4", "0.7"));
    }

    @Test
    public void testInvalidNumericGreaterThan() {
        Assert.assertFalse("Expected invalid number in needle not to match (but not error)", StringMatchType.NUMERIC_GREATER_THAN.matches("foo", "0"));
        Assert.assertFalse("Expected invalid number in haystack not to match (but not error)", StringMatchType.NUMERIC_GREATER_THAN.matches("0", "foo"));
    }

    @Test
    public void testNumericLessThan() {
        Assert.assertFalse("Expected identical numbers not to match", StringMatchType.NUMERIC_LESS_THAN.matches("0", "0"));
        Assert.assertFalse("Expected less than not to match", StringMatchType.NUMERIC_LESS_THAN.matches("50", "0.3"));
        Assert.assertTrue("Expected less than to match", StringMatchType.NUMERIC_LESS_THAN.matches("0.4", "0.7"));
    }

    @Test
    public void testInvalidNumericLessThan() {
        Assert.assertFalse("Expected invalid number in needle not to match (but not error)", StringMatchType.NUMERIC_LESS_THAN.matches("foo", "0"));
        Assert.assertFalse("Expected invalid number in haystack not to match (but not error)", StringMatchType.NUMERIC_LESS_THAN.matches("0", "foo"));
    }
}
