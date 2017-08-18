package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;
import static com.funnelback.common.facetednavigation.models.FacetValues.*;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition.FacetSearchData;
import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryValueComputedDataHolder;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

import lombok.Setter;

public class CollectionFillTest {
    
    public class TestableCollectionFill extends CollectionFill {
        
        @Setter private FacetSearchData facetSearchData;

        public TestableCollectionFill(String categoryName, List<String> collections) {
            super(categoryName, collections);
        }
        
        protected FacetSearchData getFacetSearchData(SearchTransaction st, FacetDefinition facetDefinition) {
            return this.facetSearchData;
        }
        
    }

    @Test
    public void testMultipleCollections() {
        SearchResponse sr = new SearchResponse();
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), sr);
        
        sr.setResultPacket(new ResultPacket());
        sr.getResultPacket().getDocumentsPerCollection().put("col1", 12L);
        sr.getResultPacket().getDocumentsPerCollection().put("col2", 24L);
        sr.getResultPacket().getDocumentsPerCollection().put("col3", 24L);
        
        TestableCollectionFill collFill = new TestableCollectionFill("See colls 0-2", asList("col0", "col1", "col2"));
        
        FacetSearchData facetSearchData = new FacetSearchData(sr, (c, v) -> Optional.of(sr), null);
        collFill.setFacetSearchData(facetSearchData);
        
        
        List<CategoryValueComputedDataHolder> values = collFill.computeData(st, null);
        Assert.assertEquals("We should see one value for the user to click on.", 1, values.size());
        
        CategoryValueComputedDataHolder value = values.get(0);
        
        Assert.assertEquals("See colls 0-2", value.getLabel());
        Assert.assertEquals(0 + 12 + 24, value.getCount() + 0);
    }
    
    @Test
    public void testUseExtraSearchForCounts() {
        SearchResponse sr = new SearchResponse();
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), sr);
        
        sr.setResultPacket(new ResultPacket());
        sr.getResultPacket().getDocumentsPerCollection().put("col1", 12L);
        sr.getResultPacket().getDocumentsPerCollection().put("col2", 24L);
        sr.getResultPacket().getDocumentsPerCollection().put("col3", 24L);
        
        TestableCollectionFill collFill = new TestableCollectionFill("See colls 0-2", asList("col0", "col1", "col2"));
        
        FacetSearchData facetSearchData = new FacetSearchData(sr, 
                (c, v) -> Optional.empty(), 
                (c,v) -> 20); // This part is where the count might come from an extra search.
        collFill.setFacetSearchData(facetSearchData);
        
        List<CategoryValueComputedDataHolder> values = collFill.computeData(st, facetWithValue(FROM_UNSCOPED_QUERY));
        Assert.assertEquals("We should see one value for the user to click on.", 1, values.size());
        
        CategoryValueComputedDataHolder value = values.get(0);
        
        Assert.assertEquals("See colls 0-2", value.getLabel());
        Assert.assertEquals(20, value.getCount() + 0);
    }
    
    @Test
    public void testUnknownCount() {
        SearchResponse sr = new SearchResponse();
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), sr);
        
        sr.setResultPacket(new ResultPacket());
        sr.getResultPacket().getDocumentsPerCollection().put("col1", 12L);
        sr.getResultPacket().getDocumentsPerCollection().put("col2", 24L);
        sr.getResultPacket().getDocumentsPerCollection().put("col3", 24L);
        
        TestableCollectionFill collFill = new TestableCollectionFill("See colls 0-2", asList("col0", "col1", "col2"));
        
        FacetSearchData facetSearchData = new FacetSearchData(sr, 
                (c, v) -> Optional.empty(), 
                (c,v) -> null); // This part is where the count might come from an extra search.
        collFill.setFacetSearchData(facetSearchData);
        
        List<CategoryValueComputedDataHolder> values = collFill.computeData(st, facetWithValue(FROM_UNSCOPED_QUERY));
        Assert.assertEquals("We should see one value for the user to click on.", 1, values.size());
        
        CategoryValueComputedDataHolder value = values.get(0);
        
        Assert.assertEquals("See colls 0-2", value.getLabel());
        Assert.assertNull(value.getCount());
    }
    
    @Test
    public void testUnknownCollections() {
        SearchResponse sr = new SearchResponse();
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), sr);
        
        sr.setResultPacket(new ResultPacket());
        sr.getResultPacket().getDocumentsPerCollection().put("col3", 24L);
        
        TestableCollectionFill collFill = new TestableCollectionFill("See colls 0-2", asList("col0", "col1", "col2"));
        
        FacetSearchData facetSearchData = new FacetSearchData(sr, 
                (c, v) -> Optional.empty(), 
                (c,v) -> null); // This part is where the count might come from an extra search.
        collFill.setFacetSearchData(facetSearchData);
        
        List<CategoryValueComputedDataHolder> values = collFill.computeData(st, facetWithValue(FROM_UNSCOPED_QUERY));
        Assert.assertEquals("We should see no values because the SearchResponse which provides values did not"
            + " contain any of the collections for this category.",
            0, values.size());
    }
    
    @Test
    public void testUnknownCollectionsInAllValuesFacet() {
        SearchResponse sr = new SearchResponse();
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), sr);
        
        sr.setResultPacket(new ResultPacket());
        sr.getResultPacket().getDocumentsPerCollection().put("col3", 24L);
        
        TestableCollectionFill collFill = new TestableCollectionFill("See colls 0-2", asList("col0", "col1", "col2"));
        
        FacetSearchData facetSearchData = new FacetSearchData(sr, 
                (c, v) -> Optional.empty(), 
                (c,v) -> null); // This part is where the count might come from an extra search.
        collFill.setFacetSearchData(facetSearchData);
        
        List<CategoryValueComputedDataHolder> values = collFill.computeData(st, facetWithValue(FROM_UNSCOPED_ALL_QUERY));
        Assert.assertEquals("We should see a value even no result came from the query.",
            1, values.size());
    }
    
    private FacetDefinition facetWithValue(FacetValues facetValue) {
        FacetDefinition facetDefinition = mock(FacetDefinition.class);
        when(facetDefinition.getFacetValues()).thenReturn(facetValue);
        return facetDefinition;
    }
}
