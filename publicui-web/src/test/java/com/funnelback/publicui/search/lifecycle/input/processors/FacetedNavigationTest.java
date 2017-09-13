package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.Stack;

import org.junit.Assert;
import org.junit.Test;

public class FacetedNavigationTest {

    @Test
    public void testSerializeRPN() {
        Stack<String> stack = new Stack<>();
        stack.push("dog");
        stack.push("cat");
        stack.push("|");
        String gscopeExpr = new FacetedNavigation().serializeRPN(stack);
        
        Assert.assertEquals("dog,cat|", gscopeExpr);
    }
}