package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.qpo;

import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

public class KeepSingleValueReducerTest {

    @Test
    public void testReduceHardcoded() {
        QueryProcessorOptionReducer<String> reducer = new KeepSingleValueReducer<String>((a, b) -> "hardcoded", "option");
        Pair<String, String> actual = reducer.reduce("option", Arrays.asList(new String[] { "a", "b", "c", "d" }));
        Assert.assertEquals(Pair.of("option", "hardcoded"), actual);
    }

    @Test
    public void testReduce() {
        QueryProcessorOptionReducer<Integer> reducer = new KeepSingleValueReducer<>((a, b) -> Math.max(a, b), "count_urls");
        Pair<String, String> actual = reducer.reduce("count_urls", Arrays.asList(new Integer[] { 10, 5, 20, 2 }));
        Assert.assertEquals(Pair.of("count_urls", "20"), actual);
    }

    @Test
    public void testSupports() {
        QueryProcessorOptionReducer<Integer> reducer = new KeepSingleValueReducer<>((a, b) -> Math.max(a, b), "count_urls");
        Assert.assertTrue(reducer.supports("count_urls"));
        Assert.assertFalse(reducer.supports("something_else"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmpty() {
        QueryProcessorOptionReducer<Integer> reducer = new KeepSingleValueReducer<>((a, b) -> Math.max(a, b), "option");
        reducer.reduce("option", Collections.emptyList());
    }

}
