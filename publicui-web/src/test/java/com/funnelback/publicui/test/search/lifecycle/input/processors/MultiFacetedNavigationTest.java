package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.common.facetednavigation.models.FacetConstraintJoin;
import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.MultiFacetedNavigation;
import com.funnelback.publicui.search.lifecycle.inputoutput.ExtraSearchesExecutor;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetExtraSearchNames;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;

import static org.mockito.Mockito.*;
import static com.funnelback.common.facetednavigation.models.FacetConstraintJoin.*;
import static com.funnelback.common.facetednavigation.models.FacetValues.*;

public class MultiFacetedNavigationTest {

    private MultiFacetedNavigation processor;
    private SearchTransaction st;
    
    private final File searchHome = new File("src/test/resources/dummy-search_home");
    
    @Before
    public void before() throws FileNotFoundException {
        processor = new MultiFacetedNavigation();
        
        Collection c = new Collection("dummy", new NoOptionsConfig(searchHome, "dummy"));
        SearchQuestion question = new SearchQuestion();
        question.setCollection(c);
        st = new SearchTransaction(question, null);
    }
    
    @Test
    public void testMissingData() throws InputProcessorException {
        // No transaction
        processor.processInput(null);
        
        // No question
        processor.processInput(new SearchTransaction(null, null));
        
        // No collection
        SearchQuestion question = new SearchQuestion();
        SearchTransaction st = new SearchTransaction(question, null);
        processor.processInput(st);
        Assert.assertEquals(0, st.getExtraSearchesQuestions().size());
    }
    
    @Test
    public void testDisabled() throws InputProcessorException {
        st.getQuestion().getCollection().getConfiguration()
            .setValue(Keys.ModernUI.FULL_FACETS_LIST, "false");
        
        processor.processInput(st);
        
        Assert.assertEquals(0, st.getExtraSearchesQuestions().size());
    }

    @Test
    public void testEnabled() throws InputProcessorException {
        st.getQuestion().getCollection().getConfiguration()
            .setValue(Keys.ModernUI.FULL_FACETS_LIST, "true");
        
        processor.processInput(st);
        
        Assert.assertEquals(1, st.getExtraSearchesQuestions().size());
        Assert.assertTrue(st.getExtraSearchesQuestions().containsKey(SearchTransaction.ExtraSearches.FACETED_NAVIGATION.toString()));
        Assert.assertNotNull(st.getExtraSearchesQuestions().get(SearchTransaction.ExtraSearches.FACETED_NAVIGATION.toString()));
    }
    
    @Test
    public void testExtraSearch() throws InputProcessorException {
        st.getQuestion().getCollection().getConfiguration()
            .setValue(Keys.ModernUI.FULL_FACETS_LIST, "true");
        st.getQuestion().setQuestionType(SearchQuestionType.EXTRA_SEARCH);
    
        processor.processInput(st);
        
        Assert.assertEquals(0, st.getExtraSearchesQuestions().size());
    }
    
    @Test
    public void doAnyFacetsNeedFullFacetValuesTest() {
        MultiFacetedNavigation multiFacetedNavigation = new MultiFacetedNavigation() {
            public List<FacetDefinition> getFacetDefinitions(SearchTransaction st) {
                return Arrays.asList(facet(AND, FROM_UNSCOPED_QUERY), facet(OR, FROM_UNSCOPED_QUERY));
            }
        };
        
        Assert.assertTrue(multiFacetedNavigation.doAnyFacetsNeedFullFacetValues(st));
    }
    
    @Test
    public void doAnyFacetsNeedFullFacetValuesNoFacets() {
        MultiFacetedNavigation multiFacetedNavigation = new MultiFacetedNavigation() {
            public List<FacetDefinition> getFacetDefinitions(SearchTransaction st) {
                return Arrays.asList();
            }
        };
        
        Assert.assertFalse(multiFacetedNavigation.doAnyFacetsNeedFullFacetValues(st));
    }
    
    @Test
    public void doAnyFacetsNeedFullFacetValuesNoOrTypeFacets() {
        MultiFacetedNavigation multiFacetedNavigation = new MultiFacetedNavigation() {
            public List<FacetDefinition> getFacetDefinitions(SearchTransaction st) {
                return Arrays.asList(facet(AND, FROM_SCOPED_QUERY));
            }
        };
        
        Assert.assertFalse(multiFacetedNavigation.doAnyFacetsNeedFullFacetValues(st));
    }
    
    @Test
    public void addExtraSearchesForOrBasedFacetCountsTest() {
        st.getQuestion().getRawInputParameters().put("removeme", new String[]{"df"});
        
        MultiFacetedNavigation multiFacetedNavigation = new MultiFacetedNavigation() {
            public List<FacetDefinition> getFacetDefinitions(SearchTransaction st) {
                return Arrays.asList(facet("not bob", AND, FROM_UNSCOPED_QUERY), facet("bob", OR, FROM_UNSCOPED_QUERY));
            }
        };
        
        //Make a dummy extra search which has executed the UNSCOPED facets
        SearchTransaction unscopedExtraSearch = mock(SearchTransaction.class, RETURNS_DEEP_STUBS);
        
        Facet andFacet = mock(Facet.class);
        when(andFacet.getName()).thenReturn("not bob");
        CategoryValue andCat = categoryValue("f.bar|ff", "a=b", "data 1");
        when(andFacet.getUnselectedValues()).thenReturn(Arrays.asList(andCat));
        
        
        Facet orFacet = mock(Facet.class);
        when(orFacet.getName()).thenReturn("bob");
        CategoryValue orCat = categoryValue("f.bar|ff", "fromor=bar", "data 2");
        when(orFacet.getUnselectedValues()).thenReturn(Arrays.asList(orCat));
        
        
        // Add both OR and AND facet to the unscoped extra search
        when(unscopedExtraSearch.getResponse().getFacets()).thenReturn(Arrays.asList(orFacet, andFacet));
        
        // Updated the ExtraSearchExecutor to mock returning the dummy unscoped extra search
        ExtraSearchesExecutor executor = mock(ExtraSearchesExecutor.class);
        when(executor.getAndMaybeWaitForExtraSearch(st, "FACETED_NAVIGATION"))
            .thenReturn(Optional.of(unscopedExtraSearch));
        
        multiFacetedNavigation.setExtraSearchesExecutor(executor);
        
        multiFacetedNavigation.addExtraSearchesForOrBasedFacetCounts(st);
        
        Assert.assertEquals("Only one facet is a OR type facet so only that one should "
            + "have an extra search made or it.",
            1, st.getExtraSearchesQuestions().size());
        
        Assert.assertTrue("Expected to see the 'bob' OR facet in the extra searches", 
            st.getExtraSearchesQuestions().containsKey(new FacetExtraSearchNames().getExtraSearchName(orFacet, orCat)));
        
        SearchQuestion questionOr = st.getExtraSearchesQuestions()
            .get(new FacetExtraSearchNames().getExtraSearchName(orFacet, orCat));
        
        Assert.assertFalse("Should have removed all URL params from the original query, as"
            + " we want to simulate running the search transaction when the OR facet is clicked", 
            questionOr.getRawInputParameters().containsKey("removeme"));
        
        Assert.assertTrue("Should have added the URL params from the the OR facet value",
            questionOr.getRawInputParameters().containsKey("fromor"));
        
        Assert.assertTrue("Should have added over ruling padre speed up options like rmcf=[]",
            questionOr.getRawInputParameters().containsKey("rmcf"));
    }
    
    private FacetDefinition facet(FacetConstraintJoin join, FacetValues values) {
        return facet(System.currentTimeMillis() + "", join, values);
    }
    
    private FacetDefinition facet(String name, FacetConstraintJoin join, FacetValues values) {
        FacetDefinition facetDef = mock(FacetDefinition.class);
        when(facetDef.getConstraintJoin()).thenReturn(join);
        when(facetDef.getFacetValues()).thenReturn(values);
        when(facetDef.getName()).thenReturn(name);
        return facetDef;
    }
    
    private CategoryValue categoryValue(String queryStringParamName, String selectUrl, String data) {
        CategoryValue catval = mock(CategoryValue.class);
        when(catval.getQueryStringParamName()).thenReturn(queryStringParamName);
        when(catval.getSelectUrl()).thenReturn(selectUrl);
        when(catval.getData()).thenReturn(data);
        return catval;
    }
    

    
}
