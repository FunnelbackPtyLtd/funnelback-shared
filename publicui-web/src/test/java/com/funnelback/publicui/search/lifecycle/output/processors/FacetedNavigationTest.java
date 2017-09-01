package com.funnelback.publicui.search.lifecycle.output.processors;

import static com.funnelback.common.facetednavigation.models.FacetValuesOrder.CATEGORY_DEFINITION_ORDER;
import static com.funnelback.common.facetednavigation.models.FacetValuesOrder.COUNT_DESCENDING;
import static com.funnelback.common.facetednavigation.models.FacetValuesOrder.LABEL_ASCENDING;
import static com.funnelback.common.facetednavigation.models.FacetValuesOrder.SELECTED_FIRST;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.facetednavigation.models.FacetConstraintJoin;
import com.funnelback.common.facetednavigation.models.FacetSelectionType;
import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.common.facetednavigation.models.FacetValuesOrder;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryValueComputedDataHolder;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.Category;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

import lombok.Setter;
import lombok.experimental.Wither;
public class FacetedNavigationTest {

    /**
     * The test does not mock Facet, FacetComparatorProvider and comparators used.
     */
    @Test
    public void testFacetSorting() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        st.getResponse().setResultPacket(new ResultPacket());
        
        FacetedNavigationConfig config = new FacetedNavigationConfig(
            asList(
                facetDefWithOrder("a", 
                    asList(new DummyCategory(getCatVal("c", 12, false), getCatVal("d", 1000, false), getCatVal("c", 34, false)),
                            new DummyCategory(getCatVal("e", 12, false), getCatVal("a", 12, false), getCatVal("z", 12, true))), 
                    SELECTED_FIRST, CATEGORY_DEFINITION_ORDER, LABEL_ASCENDING, COUNT_DESCENDING)));
        Collection collection = mock(Collection.class);
        when(collection.getFacetedNavigationConfConfig()).thenReturn(config);
        st.getQuestion().setCollection(collection);
        new FacetedNavigation().processOutput(st);
        
        List<CategoryValue> values = st.getResponse().getFacets().get(0).getAllValues();
        
        Assert.assertEquals(6, values.size());
        
        Assert.assertEquals("z is first as it is selected.", "z", values.get(0).getLabel());
        // After this we expected ordering to be in the category definition order.
        Assert.assertEquals("c comes before d within the category definition", "c", values.get(1).getLabel());
        Assert.assertEquals(34, values.get(1).getCount() + 0);
        Assert.assertEquals("c", values.get(2).getLabel());
        Assert.assertEquals("d", values.get(3).getLabel());
        
        Assert.assertEquals("a", values.get(4).getLabel());
        Assert.assertEquals("e", values.get(5).getLabel());
        
        // Also check that we set a depth on all values.
        values.stream()
            .forEach(v -> Assert.assertEquals("Depth was not set on value: " + v.getLabel(), 0, v.getCategoryDepth()));
    }
    
    /**
     * test sorting in drill down facets if we have something like:
     * Earth -> Australia:
     * -> ACT
     * -> NSW
     * -> VIC
     * 
     * We have selected the planet earth then the country Australia, we want
     * the selected items to be ordered Earth then Aus, but the unselected
     * values should be sorted in alphabetical order
     */
    @Test
    public void testFacetSortingSelectedDrillDown() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        st.getResponse().setResultPacket(new ResultPacket());
        
        DummyCategory topCat = new DummyCategory(getCatVal("Earth", 12, true));
        topCat.getSubCategories().add(new DummyCategory(getCatVal("Australia", 12, true)));
        topCat.getSubCategories().get(0).getSubCategories()
            .add(new DummyCategory(getCatVal("ACT", 12, false)));
        
        FacetedNavigationConfig config = new FacetedNavigationConfig(
            asList(
                facetDefWithOrder("a", 
                    asList(topCat), 
                    SELECTED_FIRST, LABEL_ASCENDING)));
        Collection collection = mock(Collection.class);
        when(collection.getFacetedNavigationConfConfig()).thenReturn(config);
        st.getQuestion().setCollection(collection);
        new FacetedNavigation().processOutput(st);
        
        List<CategoryValue> values = st.getResponse().getFacets().get(0).getAllValues();
        
        Assert.assertEquals(3, values.size());
        
        // Check each value has the correct depth
        Assert.assertEquals(0, values.get(0).getCategoryDepth());
        Assert.assertEquals(1, values.get(1).getCategoryDepth());
        Assert.assertEquals(2, values.get(2).getCategoryDepth());
        
        Assert.assertEquals("Earth", values.get(0).getLabel());
        Assert.assertEquals("Australia", values.get(1).getLabel());
        Assert.assertEquals("ACT", values.get(2).getLabel());
    }
    
    /**
     * This tests the URLFill case where a single category is retuning nested values.
     * That is you setup a single category e.g. the single URLFill
     * but it will can supply all of the follownig values:
     * home -> user
     * -> desktop
     * -> downloads
     * it supplied the selected home and user values as well as the unselected
     * nested values desktop and downloads.
     */
    @Test
    public void testFacetSortingWhenTheCategoriesValuesAreNested() {
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        st.getResponse().setResultPacket(new ResultPacket());
        
        FacetedNavigationConfig config = new FacetedNavigationConfig(
            asList(
                facetDefWithOrder("a", 
                    asList(new DummyCategory(getCatVal("c", 12, true), getCatVal("b", 1000, true), getCatVal("a", 34, true))
                            .withSelectedValuesAreNested(true)), 
                    SELECTED_FIRST, CATEGORY_DEFINITION_ORDER, LABEL_ASCENDING, COUNT_DESCENDING)));
        Collection collection = mock(Collection.class);
        when(collection.getFacetedNavigationConfConfig()).thenReturn(config);
        st.getQuestion().setCollection(collection);
        new FacetedNavigation().processOutput(st);
        
        List<CategoryValue> values = st.getResponse().getFacets().get(0).getAllValues();
        
        Assert.assertEquals(3, values.size());
        
        // Check each value has the correct depth
        Assert.assertEquals(0, values.get(0).getCategoryDepth());
        Assert.assertEquals(1, values.get(1).getCategoryDepth());
        Assert.assertEquals(2, values.get(2).getCategoryDepth());
        
        Assert.assertEquals("c", values.get(0).getLabel());
        Assert.assertEquals("b", values.get(1).getLabel());
        Assert.assertEquals("a", values.get(2).getLabel());
    }
    
    @Test
    public void removingUnselectedValuesForDrillDownFacets() {
        Facet facet = mock(Facet.class);
        when(facet.getFacetValues()).thenReturn(FacetValues.FROM_SCOPED_QUERY_HIDE_UNSELECTED_PARENT_VALUES);
        
        Category towns = new Category("towns", "");
        towns.getValues().add(categoryValue("cowra", false));
        towns.getValues().add(categoryValue("parkes", false));
        
        Category states = new Category("states", "");
        states.getCategories().add(towns);
        states.getValues().add(categoryValue("NSW", false));
        states.getValues().add(categoryValue("VIC", true));
        states.getValues().add(categoryValue("QLD", false));
        
        Category territories = new Category("territories", "");
        territories.getValues().add(categoryValue("ACT", false));
        territories.getValues().add(categoryValue("NT", false));
        
        Category countries = new Category("countries", "");
        countries.getCategories().add(states);
        countries.getValues().add(categoryValue("Ar", false));
        countries.getValues().add(categoryValue("Aus", true));
        countries.getValues().add(categoryValue("Nz", false));
        
        when(facet.getCategories()).thenReturn(asList(countries));
        
        new FacetedNavigation().removeUnslectedValuesForDrillDownFacets(facet);
        
        Assert.assertEquals(1, countries.getValues().size());
        Assert.assertEquals("Aus", countries.getValues().get(0).getLabel());
        
        Assert.assertEquals(1, states.getValues().size());
        Assert.assertEquals("VIC", states.getValues().get(0).getLabel());
        
        Assert.assertEquals(2, towns.getValues().size());
    }
    
    public CategoryValue categoryValue(String label, boolean selected) {
        CategoryValue value = mock(CategoryValue.class);
        when(value.getData()).thenReturn(label);
        when(value.getLabel()).thenReturn(label);
        when(value.isSelected()).thenReturn(selected);
        return value;
    }
    
    public class DummyCategory extends CategoryDefinition {
        
        @Setter private List<CategoryValueComputedDataHolder> data;
        
        @Wither private boolean selectedValuesAreNested = false;
        
        public DummyCategory(CategoryValueComputedDataHolder ... data) {
            super("dummy");
            this.data = asList(data);
        }
        
        public DummyCategory(List<CategoryValueComputedDataHolder> data, boolean selectedValuesAreNested) {
            super("dummy");
            this.data = data;
            this.selectedValuesAreNested = selectedValuesAreNested;
        }

        @Override
        public List<CategoryValueComputedDataHolder> computeData(SearchTransaction st, FacetDefinition fdef) {
            return data;
        }

        @Override
        public String getQueryStringCategoryExtraPart() {
            return "dummy";
        }

        @Override
        public boolean matches(String value, String extraParams) {
            throw new NotImplementedException("");
        }

        @Override
        public List<QueryProcessorOption<?>> getQueryProcessorOptions(SearchQuestion question) {
            throw new NotImplementedException("");
        }

        @Override
        public boolean allValuesDefinedByUser() {
            throw new NotImplementedException("");
        }

        @Override
        public boolean selectedValuesAreNested() {
            return this.selectedValuesAreNested;
        }        
    }
    
    public CategoryValueComputedDataHolder getCatVal(String label, int count, boolean selected) {
        return new CategoryValueComputedDataHolder(label, label, count, "constraint", selected, "dummy", label);
    }
    
    public FacetDefinition facetDefWithOrder(String name, List<CategoryDefinition> categories, FacetValuesOrder ... order) {
        return new FacetDefinition(name, 
            categories, 
            FacetSelectionType.SINGLE, 
            FacetConstraintJoin.AND, 
            FacetValues.FROM_SCOPED_QUERY, 
            asList(order));
    }
}
