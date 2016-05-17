package com.funnelback.publicui.test.search.lifecycle.output.processors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.lifecycle.output.processors.FacetedNavigation;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;

public class FacetedNavigationFacetSortingTests {

    @Test
    public void testSorting() throws Exception {
        List<FacetDefinition> list = new ArrayList<>();
        list.add(new FacetDefinition("blue", null));
        list.add(new FacetDefinition("red", null));
        list.add(new FacetDefinition("green", null));
        list.add(new FacetDefinition("yellow", null));

        List<FacetDefinition> sortedList = new FacetedNavigation().sortFacetDefinitions(list,
            Arrays.asList(new String[] { "yellow", "red", "blue" }));

        Assert.assertTrue(sortedList.get(0).getName().equals("yellow"));
        Assert.assertTrue(sortedList.get(1).getName().equals("red"));
        Assert.assertTrue(sortedList.get(2).getName().equals("blue"));
        Assert.assertTrue(sortedList.get(3).getName().equals("green"));
    }

    @Test
    public void testSortStability() throws Exception {
        // The order of the file should be preserved in the absence of a sort order for now
        // Later, we likely want to shuffle the initial order, to avoid people relying on the file order.
        List<FacetDefinition> list = new ArrayList<>();
        list.add(new FacetDefinition("blue", null));
        list.add(new FacetDefinition("red", null));
        list.add(new FacetDefinition("green", null));
        list.add(new FacetDefinition("yellow", null));

        List<FacetDefinition> sortedList = new FacetedNavigation().sortFacetDefinitions(list,
            Arrays.asList(new String[] {}));

        Assert.assertTrue(sortedList.get(0).getName().equals("blue"));
        Assert.assertTrue(sortedList.get(1).getName().equals("red"));
        Assert.assertTrue(sortedList.get(2).getName().equals("green"));
        Assert.assertTrue(sortedList.get(3).getName().equals("yellow"));
    }

}