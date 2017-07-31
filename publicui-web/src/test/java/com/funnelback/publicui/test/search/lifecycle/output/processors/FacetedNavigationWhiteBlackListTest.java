package com.funnelback.publicui.test.search.lifecycle.output.processors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.common.facetednavigation.models.FacetConstraintJoin;
import com.funnelback.common.facetednavigation.models.FacetSelectionType;
import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.FacetedNavigationWhiteBlackList;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.Category;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class FacetedNavigationWhiteBlackListTest {

    private SearchTransaction st;
    
    private FacetedNavigationWhiteBlackList processor;

    @Before
    public void before() throws Exception {
        Category ct = new Category("Category Type", "");
        ct.getValues().add(new Facet.CategoryValue("value1", "category1", 5, "a=b", "a", false));
        ct.getValues().add(new Facet.CategoryValue("value2", "category2", 10, "c=d", "c", false));
        ct.getValues().add(new Facet.CategoryValue("value3", "category3", 13, "e=f", "e", false));
        
        Facet f = new Facet("Test Facet", FacetSelectionType.SINGLE, FacetConstraintJoin.LEGACY, FacetValues.FROM_SCOPED_QUERY);
        f.getCategories().add(ct);
        
        SearchResponse sr = new SearchResponse();
        sr.getFacets().add(f);
        
        SearchQuestion sq = new SearchQuestion();
        sq.setCollection(new Collection("dummy", new NoOptionsConfig("dummy")));
        st = new SearchTransaction(sq, sr);
        
        processor = new FacetedNavigationWhiteBlackList();
    }
    
    @Test
    public void testNoWhiteOrBlackList() throws OutputProcessorException {
        processor.processOutput(st);
        
        Assert.assertEquals(1, st.getResponse().getFacets().size());
        Assert.assertEquals("Test Facet", st.getResponse().getFacets().get(0).getName());
        Assert.assertEquals(1, st.getResponse().getFacets().get(0).getCategories().size());
        Assert.assertEquals(3, st.getResponse().getFacets().get(0).getCategories().get(0).getValues().size());
    }
    
    @Test
    public void testWhiteList() throws OutputProcessorException {
        st.getQuestion().getCollection().getConfiguration()
            .setValue(Keys.FacetedNavigation.WHITE_LIST + ".Test Facet", "cAteGory2,CategOry3");

        processor.processOutput(st);
        
        Assert.assertEquals(1, st.getResponse().getFacets().size());
        Assert.assertEquals("Test Facet", st.getResponse().getFacets().get(0).getName());
        Assert.assertEquals(1, st.getResponse().getFacets().get(0).getCategories().size());
        Assert.assertEquals(2, st.getResponse().getFacets().get(0).getCategories().get(0).getValues().size());
        
        Assert.assertEquals(0, CollectionUtils.countMatches(
                st.getResponse().getFacets().get(0).getCategories().get(0).getValues(),
                new CategoryValueLabelPredicate("category1")));
        Assert.assertEquals(1, CollectionUtils.countMatches(
                st.getResponse().getFacets().get(0).getCategories().get(0).getValues(),
                new CategoryValueLabelPredicate("category2")));
        Assert.assertEquals(1, CollectionUtils.countMatches(
                st.getResponse().getFacets().get(0).getCategories().get(0).getValues(),
                new CategoryValueLabelPredicate("category3")));        
    }
    
    @Test
    public void testBlackList() throws OutputProcessorException {
        st.getQuestion().getCollection().getConfiguration()
            .setValue(Keys.FacetedNavigation.BLACK_LIST + ".Test Facet", "cAteGory2,CategOry3");

        processor.processOutput(st);
        
        Assert.assertEquals(1, st.getResponse().getFacets().size());
        Assert.assertEquals("Test Facet", st.getResponse().getFacets().get(0).getName());
        Assert.assertEquals(1, st.getResponse().getFacets().get(0).getCategories().size());
        Assert.assertEquals(1, st.getResponse().getFacets().get(0).getCategories().get(0).getValues().size());
        
        Assert.assertEquals(1, CollectionUtils.countMatches(
                st.getResponse().getFacets().get(0).getCategories().get(0).getValues(),
                new CategoryValueLabelPredicate("category1")));
        Assert.assertEquals(0, CollectionUtils.countMatches(
                st.getResponse().getFacets().get(0).getCategories().get(0).getValues(),
                new CategoryValueLabelPredicate("category2")));
        Assert.assertEquals(0, CollectionUtils.countMatches(
                st.getResponse().getFacets().get(0).getCategories().get(0).getValues(),
                new CategoryValueLabelPredicate("category3")));        
    }
    
    @Test
    public void testWhiteAndBlackList() throws OutputProcessorException {
        st.getQuestion().getCollection().getConfiguration()
            .setValue(Keys.FacetedNavigation.WHITE_LIST + ".Test Facet", "cAteGory2,CategOry3")
            .setValue(Keys.FacetedNavigation.BLACK_LIST + ".Test Facet", "cAteGory2");

        processor.processOutput(st);
        
        Assert.assertEquals(1, st.getResponse().getFacets().size());
        Assert.assertEquals("Test Facet", st.getResponse().getFacets().get(0).getName());
        Assert.assertEquals(1, st.getResponse().getFacets().get(0).getCategories().size());
        Assert.assertEquals(1, st.getResponse().getFacets().get(0).getCategories().get(0).getValues().size());
        
        Assert.assertEquals(0, CollectionUtils.countMatches(
                st.getResponse().getFacets().get(0).getCategories().get(0).getValues(),
                new CategoryValueLabelPredicate("category1")));
        Assert.assertEquals(0, CollectionUtils.countMatches(
                st.getResponse().getFacets().get(0).getCategories().get(0).getValues(),
                new CategoryValueLabelPredicate("category2")));
        Assert.assertEquals(1, CollectionUtils.countMatches(
                st.getResponse().getFacets().get(0).getCategories().get(0).getValues(),
                new CategoryValueLabelPredicate("category3")));
        
    }

    @Test
    public void testNoFacetMatching() throws OutputProcessorException {
        st.getQuestion().getCollection().getConfiguration()
            .setValue(Keys.FacetedNavigation.WHITE_LIST + ".UnknownFacet", "cAteGory2,CategOry3")
            .setValue(Keys.FacetedNavigation.BLACK_LIST + ".UnknownFacet", "cAteGory2");

        processor.processOutput(st);
        
        Assert.assertEquals(1, st.getResponse().getFacets().size());
        Assert.assertEquals("Test Facet", st.getResponse().getFacets().get(0).getName());
        Assert.assertEquals(1, st.getResponse().getFacets().get(0).getCategories().size());
        Assert.assertEquals(3, st.getResponse().getFacets().get(0).getCategories().get(0).getValues().size());
        
        Assert.assertEquals(1, CollectionUtils.countMatches(
                st.getResponse().getFacets().get(0).getCategories().get(0).getValues(),
                new CategoryValueLabelPredicate("category1")));
        Assert.assertEquals(1, CollectionUtils.countMatches(
                st.getResponse().getFacets().get(0).getCategories().get(0).getValues(),
                new CategoryValueLabelPredicate("category2")));
        Assert.assertEquals(1, CollectionUtils.countMatches(
                st.getResponse().getFacets().get(0).getCategories().get(0).getValues(),
                new CategoryValueLabelPredicate("category3")));
        
    }
    
    @Test
    public void testGlobalBlackList() throws OutputProcessorException {
        st.getQuestion().getCollection().getConfiguration()
            .setValue(Keys.FacetedNavigation.BLACK_LIST, "cAteGory2,CategOry3");

        processor.processOutput(st);
        
        Assert.assertEquals(1, st.getResponse().getFacets().size());
        Assert.assertEquals("Test Facet", st.getResponse().getFacets().get(0).getName());
        Assert.assertEquals(1, st.getResponse().getFacets().get(0).getCategories().size());
        Assert.assertEquals(1, st.getResponse().getFacets().get(0).getCategories().get(0).getValues().size());
        
        Assert.assertEquals(1, CollectionUtils.countMatches(
                st.getResponse().getFacets().get(0).getCategories().get(0).getValues(),
                new CategoryValueLabelPredicate("category1")));
        Assert.assertEquals(0, CollectionUtils.countMatches(
                st.getResponse().getFacets().get(0).getCategories().get(0).getValues(),
                new CategoryValueLabelPredicate("category2")));
        Assert.assertEquals(0, CollectionUtils.countMatches(
                st.getResponse().getFacets().get(0).getCategories().get(0).getValues(),
                new CategoryValueLabelPredicate("category3")));        
    }

    @Test
    public void testGlobalWhiteList() throws OutputProcessorException {
        st.getQuestion().getCollection().getConfiguration()
            .setValue(Keys.FacetedNavigation.WHITE_LIST, "cAteGory2,CategOry3");

        processor.processOutput(st);
        
        Assert.assertEquals(1, st.getResponse().getFacets().size());
        Assert.assertEquals("Test Facet", st.getResponse().getFacets().get(0).getName());
        Assert.assertEquals(1, st.getResponse().getFacets().get(0).getCategories().size());
        Assert.assertEquals(2, st.getResponse().getFacets().get(0).getCategories().get(0).getValues().size());
        
        Assert.assertEquals(0, CollectionUtils.countMatches(
                st.getResponse().getFacets().get(0).getCategories().get(0).getValues(),
                new CategoryValueLabelPredicate("category1")));
        Assert.assertEquals(1, CollectionUtils.countMatches(
                st.getResponse().getFacets().get(0).getCategories().get(0).getValues(),
                new CategoryValueLabelPredicate("category2")));
        Assert.assertEquals(1, CollectionUtils.countMatches(
                st.getResponse().getFacets().get(0).getCategories().get(0).getValues(),
                new CategoryValueLabelPredicate("category3")));        
    }
    
    @Test
    public void testGlobalAndNonGlobal() throws OutputProcessorException {
        st.getQuestion().getCollection().getConfiguration()
        .setValue(Keys.FacetedNavigation.WHITE_LIST+".Test Facet", "cAteGory2")
        .setValue(Keys.FacetedNavigation.WHITE_LIST, "CategOry3");

    processor.processOutput(st);
    
    Assert.assertEquals(1, st.getResponse().getFacets().size());
    Assert.assertEquals("Test Facet", st.getResponse().getFacets().get(0).getName());
    Assert.assertEquals(1, st.getResponse().getFacets().get(0).getCategories().size());
    Assert.assertEquals(2, st.getResponse().getFacets().get(0).getCategories().get(0).getValues().size());
    
    Assert.assertEquals(0, CollectionUtils.countMatches(
            st.getResponse().getFacets().get(0).getCategories().get(0).getValues(),
            new CategoryValueLabelPredicate("category1")));
    Assert.assertEquals(1, CollectionUtils.countMatches(
            st.getResponse().getFacets().get(0).getCategories().get(0).getValues(),
            new CategoryValueLabelPredicate("category2")));
    Assert.assertEquals(1, CollectionUtils.countMatches(
            st.getResponse().getFacets().get(0).getCategories().get(0).getValues(),
            new CategoryValueLabelPredicate("category3")));                
    }

    private class CategoryValueLabelPredicate implements Predicate {
        
        private final String value;

        public CategoryValueLabelPredicate(String value) {
            this.value = value;
        }
        
        @Override
        public boolean evaluate(Object object) {
            CategoryValue cv = (CategoryValue) object;
            return value.equals(cv.getLabel());
        }
        
    }
}
