package com.funnelback.publicui.search.model.transaction.facet;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.common.function.StreamUtils;

import static com.funnelback.publicui.search.model.transaction.facet.FacetDisplayType.getType;
import static com.funnelback.publicui.search.model.transaction.facet.FacetDisplayType.*;
import static com.funnelback.common.facetednavigation.models.FacetSelectionType.*;
import static com.funnelback.common.facetednavigation.models.FacetConstraintJoin.*;
import static com.funnelback.common.facetednavigation.models.FacetValues.*;

public class FacetDisplayTypeTest {

    @Test
    public void testCheckBox() {
        Assert.assertEquals(CHECKBOX, getType(MULTIPLE, AND, FROM_SCOPED_QUERY));
        Assert.assertEquals(CHECKBOX, getType(MULTIPLE, OR, FROM_SCOPED_QUERY));
        StreamUtils.ofNullable(FacetValues.values())
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
