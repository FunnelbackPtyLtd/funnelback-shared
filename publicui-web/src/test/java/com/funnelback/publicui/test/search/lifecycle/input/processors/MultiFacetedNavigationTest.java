package com.funnelback.publicui.test.search.lifecycle.input.processors;

import static com.funnelback.common.facetednavigation.models.FacetConstraintJoin.AND;
import static com.funnelback.common.facetednavigation.models.FacetConstraintJoin.OR;
import static com.funnelback.common.facetednavigation.models.FacetSelectionType.MULTIPLE;
import static com.funnelback.common.facetednavigation.models.FacetValues.FROM_SCOPED_QUERY;
import static com.funnelback.common.facetednavigation.models.FacetValues.FROM_UNSCOPED_ALL_QUERY;
import static com.funnelback.common.facetednavigation.models.FacetValues.FROM_UNSCOPED_QUERY;
import static com.funnelback.publicui.search.model.collection.facetednavigation.FacetExtraSearchNames.SEARCH_FOR_ALL_VALUES;
import static com.funnelback.publicui.search.model.collection.facetednavigation.FacetExtraSearchNames.SEARCH_FOR_UNSCOPED_VALUES;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

import com.funnelback.common.config.Keys;
import com.funnelback.common.config.NoOptionsConfig;
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
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.FacetedNavigationUtils;
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
        st.getQuestion().setQuestionType(SearchQuestionType.FACETED_NAVIGATION_EXTRA_SEARCH);
    
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
            
            @Override
            public String valueProvidingSearchName(FacetDefinition facetDef) {
                return "Name of extra search supplying values";
            }
        };
        
        //Make a dummy extra search which has executed the UNSCOPED facets
        SearchTransaction extraSearchSupplyingValues = mock(SearchTransaction.class, RETURNS_DEEP_STUBS);
        
        Facet andFacet = mock(Facet.class);
        when(andFacet.getName()).thenReturn("not bob");
        CategoryValue andCat = categoryValue("f.bar|ff", false, "a=b", "data 1");
        when(andFacet.getAllValues()).thenReturn(Arrays.asList(andCat));
        
        
        Facet orFacet = mock(Facet.class);
        when(orFacet.getName()).thenReturn("bob");
        CategoryValue orCat = categoryValue("f.bar|ff", false, "fromor=bar", "data 2");
        when(orFacet.getAllValues()).thenReturn(Arrays.asList(orCat));
        
        
        // Add both OR and AND facet to the unscoped extra search
        when(extraSearchSupplyingValues.getResponse().getFacets()).thenReturn(Arrays.asList(orFacet, andFacet));
        
        // Updated the ExtraSearchExecutor to mock returning the dummy unscoped extra search
        ExtraSearchesExecutor executor = mock(ExtraSearchesExecutor.class);
        when(executor.getAndMaybeWaitForExtraSearch(st, "Name of extra search supplying values"))
            .thenReturn(Optional.of(extraSearchSupplyingValues));
        
        multiFacetedNavigation.setExtraSearchesExecutor(executor);
        
        ServiceConfigReadOnly profileConfig = mock(ServiceConfigReadOnly.class);
        
        doReturn(profileConfig).when(st.getQuestion()).getCurrentProfileConfig();
        
        when(profileConfig.get(com.funnelback.config.keys.Keys.FrontEndKeys.ModernUi.MAX_FACET_EXTRA_SEARCHES))
            .thenReturn(0)
            .thenReturn(100);
        
        multiFacetedNavigation.addDedicatedExtraSearchesForOrFacetCounts(st);
        
        // We should have no searches as config was initially set to allow 0 extra searches
        Assert.assertEquals(0, st.getExtraSearchesQuestions().size());
        
        Assert.assertTrue(st.isAnyExtraSearchesIncomplete());
        
        st.setAnyExtraSearchesIncomplete(false);
        
        // Run the search again this time config allows extra searches
        multiFacetedNavigation.addDedicatedExtraSearchesForOrFacetCounts(st);
        Assert.assertFalse(st.isAnyExtraSearchesIncomplete());
        
        Assert.assertEquals("Only one facet is a OR type facet so only that one should "
            + "have an extra search made or it.",
            1, st.getExtraSearchesQuestions().size());
        
        Assert.assertTrue("Expected to see the 'bob' OR facet in the extra searches", 
            st.getExtraSearchesQuestions().containsKey(new FacetExtraSearchNames().extraSearchToCalculateCounOfCategoryValue(orFacet, orCat)));
        
        SearchQuestion questionOr = st.getExtraSearchesQuestions()
            .get(new FacetExtraSearchNames().extraSearchToCalculateCounOfCategoryValue(orFacet, orCat));
        
        Assert.assertFalse("Should have removed all URL params from the original query, as"
            + " we want to simulate running the search transaction when the OR facet is clicked", 
            questionOr.getRawInputParameters().containsKey("removeme"));
        
        Assert.assertTrue("Should have added the URL params from the the OR facet value",
            questionOr.getRawInputParameters().containsKey("fromor"));
        
        Assert.assertTrue("Should have added over ruling padre speed up options like rmcf=[]",
            questionOr.getPriorityQueryProcessorOptions().getOptions().containsKey("rmcf"));
    }
    
    @Test
    public void valueProvidingSearchNameTestFROM_UNSCOPED_QUERY() {
        FacetDefinition facetDefUnscoped = mock(FacetDefinition.class);
        when(facetDefUnscoped.getFacetValues()).thenReturn(FacetValues.FROM_UNSCOPED_QUERY);
        
        Assert.assertEquals(SEARCH_FOR_UNSCOPED_VALUES, 
            new MultiFacetedNavigation().valueProvidingSearchName(facetDefUnscoped));
    }
    
    @Test
    public void valueProvidingSearchNameTestFROM_UNSCOPED_ALL_QUERY() {
        FacetDefinition facetDefUnscopedAll = mock(FacetDefinition.class);
        when(facetDefUnscopedAll.getFacetValues()).thenReturn(FacetValues.FROM_UNSCOPED_ALL_QUERY);
        
        Assert.assertEquals(SEARCH_FOR_ALL_VALUES, 
            new MultiFacetedNavigation().valueProvidingSearchName(facetDefUnscopedAll));
    }
    
    @Test
    public void valueProvidingSearchNameTestFROM_SCOPED_QUERY_WITH_FACET_UNSELECTED() {
        FacetDefinition facetDefUnscopedAll = mock(FacetDefinition.class);
        when(facetDefUnscopedAll.getFacetValues()).thenReturn(FacetValues.FROM_SCOPED_QUERY_WITH_FACET_UNSELECTED);
        
        FacetExtraSearchNames facetExtraSearchNames = mock(FacetExtraSearchNames.class);
        when(facetExtraSearchNames.extraSearchWithFacetUnchecked(facetDefUnscopedAll))
            .thenReturn("Foo");
        
        MultiFacetedNavigation multiFacetedNavigation = new MultiFacetedNavigation();
        multiFacetedNavigation.setFacetExtraSearchNames(facetExtraSearchNames);
        
        Assert.assertEquals("Foo", 
            multiFacetedNavigation.valueProvidingSearchName(facetDefUnscopedAll));
    }
    
    @Test
    public void valueProvidingSearchNameTestOther() {
        FacetDefinition facetDefUnscopedAll = mock(FacetDefinition.class);
        when(facetDefUnscopedAll.getFacetValues()).thenReturn(FacetValues.FROM_SCOPED_QUERY);
        
        try {
            new MultiFacetedNavigation().valueProvidingSearchName(facetDefUnscopedAll);
            Assert.fail("FROM_SCOPED_QUERY should be rejected as it is not one we support "
                + "running dedicated searches to work out the count."); 
        } catch (IllegalStateException e) {
        }
    }
    
    @Test
    public void testAddExtraSearchToGetUnscopedValues() throws Exception {
        SearchTransaction searchTransaction = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        new MultiFacetedNavigation().addExtraSearchToGetUnscopedValues(searchTransaction);
        SearchQuestion extraSearch = searchTransaction.getExtraSearchesQuestions().get(SEARCH_FOR_UNSCOPED_VALUES);
        Assert.assertNotNull("The unscoped extra search is missing are missing", extraSearch);
        
        Assert.assertFalse("Should not have overridden rmcf (to turn it off) or other options "
            + "needed by faceted nav", 
            extraSearch.getPriorityQueryProcessorOptions().getOptions().containsKey("rmcf"));
        
        Assert.assertTrue("Should have turned of features not needed by faceted nav", 
            extraSearch.getPriorityQueryProcessorOptions().getOptions().containsKey("SSS"));
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
    
    @Test
    public void testAddScopedSearchWithFacetUnselected() {
        FacetDefinition facet = facet("foobar", AND, FROM_UNSCOPED_ALL_QUERY, MULTIPLE);
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        st.getQuestion().setQuery("query");
        st.getQuestion().getInputParameterMap().put("f.foobar|blah", "blah");
        st.getQuestion().getInputParameterMap().put("f.other|blah", "blah");
        st.getQuestion().getInputParameterMap().put("facetScope", "f.foobar%7Cblah%3Dblah");
        processor.addScopedSearchWithFacetUnselected(st, facet);
        Assert.assertEquals("Missing extra search", 1, st.getExtraSearchesQuestions().size());
        Assert.assertNotNull(st.getExtraSearchesQuestions().get("INTERNAL_FACETED_NAV_SEARCH_FACET_DISABLED-foobar"));
        SearchQuestion searchQuestion = st.getExtraSearchesQuestions().get("INTERNAL_FACETED_NAV_SEARCH_FACET_DISABLED-foobar");
        Assert.assertEquals("query should stay enabled","query", searchQuestion.getQuery());
        Assert.assertFalse("Counting options should not have been disabled",
            searchQuestion.getPriorityQueryProcessorOptions().getOptions().containsKey("rmcf"));
        Assert.assertTrue("Should have kept other facets on", 
            searchQuestion.getRawInputParameters().containsKey("f.other|blah"));
        
        Assert.assertFalse("Should have unchecked the given facet", 
            searchQuestion.getRawInputParameters().containsKey("f.foobar|blah"));
        
        Assert.assertFalse("Should have unchecked the given facet from facetScope", 
            searchQuestion.getRawInputParameters().containsKey("facetScope"));
        
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
    
    private CategoryValue categoryValue(String queryStringParamName, boolean selected, String toggleUrl, String data) {
        CategoryValue catval = mock(CategoryValue.class);
        when(catval.getQueryStringParamName()).thenReturn(queryStringParamName);
        when(catval.isSelected()).thenReturn(selected);
        when(catval.getToggleUrl()).thenReturn(toggleUrl);
        when(catval.getData()).thenReturn(data);
        return catval;
    }
    

    
}
