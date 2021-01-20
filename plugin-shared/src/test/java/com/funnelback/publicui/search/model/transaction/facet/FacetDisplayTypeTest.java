package com.funnelback.publicui.search.model.transaction.facet;

import static com.funnelback.common.facetednavigation.models.FacetConstraintJoin.AND;
import static com.funnelback.common.facetednavigation.models.FacetConstraintJoin.OR;
import static com.funnelback.common.facetednavigation.models.FacetSelectionType.MULTIPLE;
import static com.funnelback.common.facetednavigation.models.FacetSelectionType.SINGLE;
import static com.funnelback.common.facetednavigation.models.FacetSelectionType.SINGLE_AND_UNSELECT_OTHER_FACETS;
import static com.funnelback.common.facetednavigation.models.FacetValues.FROM_SCOPED_QUERY;
import static com.funnelback.common.facetednavigation.models.FacetValues.FROM_SCOPED_QUERY_HIDE_UNSELECTED_PARENT_VALUES;
import static com.funnelback.common.facetednavigation.models.FacetValues.FROM_UNSCOPED_ALL_QUERY;
import static com.funnelback.common.facetednavigation.models.FacetValues.FROM_UNSCOPED_QUERY;
import static com.funnelback.publicui.search.model.transaction.facet.FacetDisplayType.CHECKBOX;
import static com.funnelback.publicui.search.model.transaction.facet.FacetDisplayType.RADIO_BUTTON;
import static com.funnelback.publicui.search.model.transaction.facet.FacetDisplayType.SINGLE_DRILL_DOWN;
import static com.funnelback.publicui.search.model.transaction.facet.FacetDisplayType.TAB;
import static com.funnelback.publicui.search.model.transaction.facet.FacetDisplayType.getType;

import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.facetednavigation.models.FacetValues;

public class FacetDisplayTypeTest {

    @Test
    public void testCheckBox() {
        Assert.assertEquals(CHECKBOX, getType(MULTIPLE, AND, FROM_SCOPED_QUERY));
        Assert.assertEquals(CHECKBOX, getType(MULTIPLE, OR, FROM_SCOPED_QUERY));
        Stream.of(FacetValues.values())
         .forEach(facetValue -> Assert.assertEquals(CHECKBOX, getType(MULTIPLE, OR, facetValue)));
    }
    
    @Test
    public void testRadio() {
        Assert.assertEquals(RADIO_BUTTON, getType(SINGLE, AND, FROM_UNSCOPED_QUERY));
        Assert.assertEquals(RADIO_BUTTON, getType(SINGLE, AND, FROM_UNSCOPED_ALL_QUERY));
    }
    
    @Test
    public void testTab() {
        Assert.assertEquals(TAB, getType(SINGLE_AND_UNSELECT_OTHER_FACETS, AND, FROM_UNSCOPED_QUERY));
        Assert.assertEquals(TAB, getType(SINGLE_AND_UNSELECT_OTHER_FACETS, AND, FROM_UNSCOPED_ALL_QUERY));
    }
    
    @Test
    public void testSingleDrillDown() {
        Assert.assertEquals(SINGLE_DRILL_DOWN, getType(SINGLE, AND, FROM_SCOPED_QUERY));
        Assert.assertEquals(SINGLE_DRILL_DOWN, getType(SINGLE, AND, FROM_SCOPED_QUERY_HIDE_UNSELECTED_PARENT_VALUES));
    }
}
