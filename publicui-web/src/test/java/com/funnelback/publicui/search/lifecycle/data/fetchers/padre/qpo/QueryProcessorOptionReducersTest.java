package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.qpo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.publicui.search.model.collection.QueryProcessorOption;

public class QueryProcessorOptionReducersTest {

    private QueryProcessorOptionReducers reducers;

    @Before
    public void before() {
        reducers = new QueryProcessorOptionReducers();
    }
    
    @Test
    public void testEmpty() {
        Assert.assertTrue(reducers.reduceAllQueryProcessorOptions(new ArrayList<>()).isEmpty());
    }

    @Test
    public void test() {
        List<QueryProcessorOption<?>> data = Arrays.asList(new QueryProcessorOption[] {
                        new QueryProcessorOption<String>("count_dates", "x"),
                        new QueryProcessorOption<String>("count_dates", "e"),
                        new QueryProcessorOption<String>("count_dates", "d"),
                        new QueryProcessorOption<String>("countgbits", "all"),
                        new QueryProcessorOption<String>("countgbits", "all"),
                        new QueryProcessorOption<Integer>("count_urls", 1),
                        new QueryProcessorOption<Integer>("count_urls", 5),
                        new QueryProcessorOption<Integer>("count_urls", 10),
                        new QueryProcessorOption<String>("rmcf", "a"),
                        new QueryProcessorOption<String>("rmcf", "a"),
                        new QueryProcessorOption<String>("rmcf", "b"),
                        new QueryProcessorOption<String>("rmcf", "c"),
                        new QueryProcessorOption<String>("unsupported", "option1"),
                        new QueryProcessorOption<String>("unsupported", "option2"),
                        new QueryProcessorOption<Long>("another", 42L),
        });

        Collections.shuffle(data);

        List<Pair<String, String>> actual = reducers.reduceAllQueryProcessorOptions(data);

        @SuppressWarnings("unchecked")
        List<Pair<String, String>> expected = Arrays.asList(new Pair[] {
                        Pair.of("count_dates", "d"),
                        Pair.of("countgbits", "all"),
                        Pair.of("count_urls", "10"),
                        Pair.of("rmcf", "[a,b,c]"),
                        // Unsupported options should be left as is
                        Pair.of("unsupported", "option1"),
                        Pair.of("unsupported", "option2"),
                        Pair.of("another", "42"),
        });

        Assert.assertEquals(
            // Order doesn't matter, we're only interested in the content
            new HashSet<Pair<String, String>>(expected),
            new HashSet<Pair<String, String>>(actual));
    }

}
