package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.qpo;

import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

public class WrappedDelimitedDistinctMultiValuesReducerTest {

    @Test
    public void testSorted() {
        WrappedDelimitedDistinctMultiValuesReducer<String> reducer = new WrappedDelimitedDistinctMultiValuesReducer<>("option");
        Pair<String, String> actual = reducer.reduce("option", Arrays.asList(new String[] { "d", "c", "a", "b" }));
        Assert.assertEquals(Pair.of("option", "[a,b,c,d]"), actual);
    }

    @Test
    public void testDuplicates() {
        WrappedDelimitedDistinctMultiValuesReducer<String> reducer = new WrappedDelimitedDistinctMultiValuesReducer<>("option");
        Pair<String, String> actual = reducer.reduce("option", Arrays.asList(new String[] { "d", "a", "c", "a", "b", "c", "d" }));
        Assert.assertEquals(Pair.of("option", "[a,b,c,d]"), actual);
    }

    @Test
    public void testSupports() {
        WrappedDelimitedDistinctMultiValuesReducer<String> reducer = new WrappedDelimitedDistinctMultiValuesReducer<>("option");
        Assert.assertTrue(reducer.supports("option"));
        Assert.assertFalse(reducer.supports("something_else"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmpty() {
        WrappedDelimitedDistinctMultiValuesReducer<String> reducer = new WrappedDelimitedDistinctMultiValuesReducer<>("option");
        reducer.reduce("option", Collections.emptyList());
    }

}
