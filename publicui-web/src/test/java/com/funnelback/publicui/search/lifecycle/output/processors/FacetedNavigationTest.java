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
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

import lombok.Setter;
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
    }
    
    public class DummyCategory extends CategoryDefinition {
        
        @Setter private List<CategoryValueComputedDataHolder> data;
        
        public DummyCategory(CategoryValueComputedDataHolder ... data) {
            super("dummy");
            this.data = asList(data);
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
