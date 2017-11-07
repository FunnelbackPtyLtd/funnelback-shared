package com.funnelback.publicui.utils;

import org.junit.Assert;
import org.junit.Test;


public class JsonPCallbackParamTest {

    @Test
    public void test() {
        Assert.assertFalse(JsonPCallbackParam.isValid("(a"));
        Assert.assertFalse(JsonPCallbackParam.isValid(")a"));
        Assert.assertFalse(JsonPCallbackParam.isValid("<"));
        Assert.assertFalse(JsonPCallbackParam.isValid(">"));
        Assert.assertFalse(JsonPCallbackParam.isValid("/"));
        Assert.assertFalse(JsonPCallbackParam.isValid("\\"));
        Assert.assertFalse(JsonPCallbackParam.isValid("\'"));
        Assert.assertFalse(JsonPCallbackParam.isValid(";"));
        Assert.assertFalse(JsonPCallbackParam.isValid("\0"));
        Assert.assertFalse(JsonPCallbackParam.isValid(" ")); //space
        Assert.assertFalse(JsonPCallbackParam.isValid(" ")); //tab
        Assert.assertFalse(JsonPCallbackParam.isValid(";"));
        Assert.assertFalse(JsonPCallbackParam.isValid("}"));
        Assert.assertFalse(JsonPCallbackParam.isValid("{"));
        
        Assert.assertTrue(JsonPCallbackParam.isValid("hello"));
        Assert.assertTrue(JsonPCallbackParam.isValid("foo23"));
        Assert.assertTrue(JsonPCallbackParam.isValid("$210"));
        Assert.assertTrue(JsonPCallbackParam.isValid("_bar"));
        Assert.assertTrue(JsonPCallbackParam.isValid("$.ajaxHandler"));
        Assert.assertTrue(JsonPCallbackParam.isValid("array_of_functions[42]"));
        Assert.assertTrue(JsonPCallbackParam.isValid("array_of_functions[\"42\"]"));
        Assert.assertTrue(JsonPCallbackParam.isValid("$.ajaxHandler[42][1].foo"));
    }
}
