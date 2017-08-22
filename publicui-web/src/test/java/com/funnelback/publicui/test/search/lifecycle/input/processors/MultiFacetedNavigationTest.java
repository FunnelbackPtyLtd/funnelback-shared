package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import static java.util.Arrays.asList;
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
import com.funnelback.common.facetednavigation.models.FacetSelectionType;
import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.MultiFacetedNavigation;
import com.funnelback.publicui.search.lifecycle.inputoutput.ExtraSearchesExecutor;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetExtraSearchNames;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.FacetedNavigationUtils;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.search.model.transaction.SearchResponse;

import static org.mockito.Mockito.*;
import static com.funnelback.common.facetednavigation.models.FacetConstraintJoin.*;
import static com.funnelback.common.facetednavigation.models.FacetValues.*;
import static com.funnelback.publicui.search.model.collection.facetednavigation.FacetExtraSearchNames.SEARCH_FOR_UNSCOPED_VALUES;
import static com.funnelback.common.facetednavigation.models.FacetSelectionType.MULTIPLE;
public class MultiFacetedNavigationTest {

    private MultiFacetedNavigation processor;
    private SearchTransaction st;
    
    private final File searchHome = new File("src/test/resources/dummy-search_home");
    
    @Before
    public void before() throws FileNotFoundException {
        processor = new MultiFacetedNavigation();
        
        Collection c = new Collection("dummy", new NoOptionsConfig(searchHome, "dummy"));
        SearchQuestion question = spy(new SearchQuestion());
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
        
        Assert.assertTrue(multiFacetedNavigation.doAnyFacetsNeedUnscopedQuery(st));
    }
    
    @Test
    public void doAnyFacetsNeedFullFacetValuesNoFacets() {
        MultiFacetedNavigation multiFacetedNavigation = new MultiFacetedNavigation() {
            public List<FacetDefinition> getFacetDefinitions(SearchTransaction st) {
                return Arrays.asList();
            }
        };
        
        Assert.assertFalse(multiFacetedNavigation.doAnyFacetsNeedUnscopedQuery(st));
    }
    
    @Test
    public void doAnyFacetsNeedFullFacetValuesNoOrTypeFacets() {
        MultiFacetedNavigation multiFacetedNavigation = new MultiFacetedNavigation() {
            public List<FacetDefinition> getFacetDefinitions(SearchTransaction st) {
                return Arrays.asList(facet(AND, FROM_SCOPED_QUERY));
            }
        };
        
        Assert.assertFalse(multiFacetedNavigation.doAnyFacetsNeedUnscopedQuery(st));
    }
    
    @Test
    public void addExtraSearchesForOrBasedFacetCountsTest() {
        st.getQuestion().getRawInputParameters().put("removeme", new String[]{"df"});
        
        // Extra searches will only be run if at least one value of the facet has been selected.
        st.getQuestion().getSelectedCategoryValues()
            .put(FacetedNavigationUtils.facetParamNamePrefix("bob"), Arrays.asList("foo"));
        
        st.getQuestion().getSelectedCategoryValues()
            .put(FacetedNavigationUtils.facetParamNamePrefix("not bob"), Arrays.asList("foo"));
        
        MultiFacetedNavigation multiFacetedNavigation = new MultiFacetedNavigation() {
            public List<FacetDefinition> getFacetDefinitions(SearchTransaction st) {
                return Arrays.asList(facet("not bob", AND, FROM_UNSCOPED_QUERY, MULTIPLE), 
                    facet("bob", OR, FROM_UNSCOPED_QUERY, MULTIPLE));
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
        when(orFacet.getAllValues()).thenReturn(Arrays.asList(orCat));
        
        
        // Add both OR and AND facet to the unscoped extra search
        when(unscopedExtraSearch.getResponse().getFacets()).thenReturn(Arrays.asList(orFacet, andFacet));
        
        // Updated the ExtraSearchExecutor to mock returning the dummy unscoped extra search
        ExtraSearchesExecutor executor = mock(ExtraSearchesExecutor.class);
        when(executor.getAndMaybeWaitForExtraSearch(st, SEARCH_FOR_UNSCOPED_VALUES))
            .thenReturn(Optional.of(unscopedExtraSearch));
        
        multiFacetedNavigation.setExtraSearchesExecutor(executor);
        
        ServiceConfigReadOnly profileConfig = mock(ServiceConfigReadOnly.class);
        
        doReturn(profileConfig).when(st.getQuestion()).getCurrentProfileConfig();
        
        when(profileConfig.get(com.funnelback.config.keys.Keys.FrontEndKeys.ModernUI.MAX_FACET_EXTRA_SEARCHES))
            .thenReturn(0)
            .thenReturn(100);
        
        multiFacetedNavigation.addExtraSearchesForOrFacetCounts(st);
        
        // We should have no searches as config was initially set to allow 0 extra searches
        Assert.assertEquals(0, st.getExtraSearchesQuestions().size());
        
        Assert.assertTrue(st.isAnyExtraSearchesIncomplete());
        
        st.setAnyExtraSearchesIncomplete(false);
        
        // Run the search again this time config allows extra searches
        multiFacetedNavigation.addExtraSearchesForOrFacetCounts(st);
        Assert.assertFalse(st.isAnyExtraSearchesIncomplete());
        
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
    
    @Test
    public void testAddExtraSearchToGetUnscopedValues() throws Exception {
        SearchTransaction searchTransaction = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        new MultiFacetedNavigation().addExtraSearchToGetUnscopedValues(searchTransaction);
        SearchQuestion extraSearch = searchTransaction.getExtraSearchesQuestions().get(SEARCH_FOR_UNSCOPED_VALUES);
        Assert.assertNotNull("The unscoped extra search is missing are missing", extraSearch);
        
        Assert.assertFalse("Should not have overridden rmcf (to turn it off) or other options "
            + "needed by faceted nav", extraSearch.getRawInputParameters().containsKey("rmcf"));
        
        Assert.assertTrue("Should have turned of features not needed by faceted nav", 
            extraSearch.getRawInputParameters().containsKey("SSS"));
    }
    
    @Test
    public void testUserDefinedCategoriesDoNotNeedAllQuery() {
        // Setup a facet where we want all values
        FacetDefinition facet = facet(AND, FROM_UNSCOPED_ALL_QUERY);
        CategoryDefinition catDef = mock(CategoryDefinition.class);
        when(catDef.allValuesDefinedByUser()).thenReturn(true);
        when(catDef.getSubCategories()).thenReturn(asList());
        
        when(facet.getCategoryDefinitions()).thenReturn(asList(catDef));
        
        boolean res = new MultiFacetedNavigation(){
            @Override
            public List<FacetDefinition> getFacetDefinitions(SearchTransaction st) {
                return asList(facet);
            }
        }.doAnyFacetsNeedAllQuery(null);
        
        Assert.assertFalse("We can get all values from the category definitions so "
            + "we don't need to run a extra search", res);
    }
    
    @Test
    public void testFacetAndCategoriesThatNeedUnscopedAllExtraSearch() {
        // Setup a facet where we want all values
        FacetDefinition facet = facet(AND, FROM_UNSCOPED_ALL_QUERY);
        CategoryDefinition catDef = mock(CategoryDefinition.class);
        when(catDef.allValuesDefinedByUser()).thenReturn(false);
        when(catDef.getSubCategories()).thenReturn(asList());
        
        when(facet.getCategoryDefinitions()).thenReturn(asList(catDef));
        
        boolean res = new MultiFacetedNavigation(){
            @Override
            public List<FacetDefinition> getFacetDefinitions(SearchTransaction st) {
                return asList(facet);
            }
        }.doAnyFacetsNeedAllQuery(null);
        
        Assert.assertTrue("The values are derived from the index so we need to"
            + " run a query to find those values", res);
    }
    
    private FacetDefinition facet(FacetConstraintJoin join, FacetValues values) {
        return facet(System.currentTimeMillis() + "", join, values, FacetSelectionType.SINGLE);
    }
    
    private FacetDefinition facet(String name, FacetConstraintJoin join, FacetValues values, FacetSelectionType selectionType) {
        FacetDefinition facetDef = mock(FacetDefinition.class);
        when(facetDef.getConstraintJoin()).thenReturn(join);
        when(facetDef.getFacetValues()).thenReturn(values);
        when(facetDef.getName()).thenReturn(name);
        when(facetDef.getSelectionType()).thenReturn(selectionType);
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
