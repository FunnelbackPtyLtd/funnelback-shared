package com.funnelback.publicui.test.search.lifecycle.data.fetchers.padre.exec;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Collection.Type;
import com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec.PadreQueryStringBuilder;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import static org.mockito.Mockito.*;
public class PadreQueryStringBuilderTest {

    SearchQuestion q;
    Config config;
    
    @Before
    public void before() {
        q = new SearchQuestion();
        config = mock(Config.class);
        q.setCollection(new Collection("dummy", config));
        q.setQuery("chocolate");
        q.getAdditionalParameters().put("a", new String[] {"1"});
    }
    
    @Test
    public void testBuildCompleteQuery() {
        SearchQuestion qs = new SearchQuestion();
        Assert.assertEquals("", new PadreQueryStringBuilder(qs, true).buildCompleteQuery());
        
        qs.setQuery("user query");
        Assert.assertEquals("user query", new PadreQueryStringBuilder(qs, true).buildCompleteQuery());
        
        qs.setQuery(null);
        qs.getMetaParameters().add("additional");
        qs.getMetaParameters().add("expr");
        Assert.assertEquals("additional expr", new PadreQueryStringBuilder(qs, true).buildCompleteQuery());

        qs.setQuery("user query");
        Assert.assertEquals("user query additional expr", new PadreQueryStringBuilder(qs, true).buildCompleteQuery());

    }
    
    @Test
    public void testNoUserEnteredQuery() {
        SearchQuestion qs = new SearchQuestion();
        qs.setCollection(new Collection("dummy", null));
        qs.setQuery(null);
        qs.getMetaParameters().add("additional expr");
        
        Assert.assertEquals("collection=dummy&profile=_default&query=additional+expr",
                new PadreQueryStringBuilder(qs, true).buildQueryString());
    }
    
    @Test
    public void testSystemMetaParametersOnly() {
        SearchQuestion qs = new SearchQuestion();
        qs.setCollection(new Collection("dummy", null));
        qs.setQuery(null);
        qs.getSystemMetaParameters().add("additional");
        qs.getSystemMetaParameters().add("meta expr");
        
        Assert.assertEquals("collection=dummy&profile=_default&s=additional+meta+expr",
                new PadreQueryStringBuilder(qs, true).buildQueryString());
    }

    @Test
    public void testSingleSystemParameters() {
        SearchQuestion qs = new SearchQuestion();
        qs.setCollection(new Collection("dummy", null));
        qs.setQuery(null);
        qs.getRawInputParameters().put("s", new String[] {"single value"});
        
        Assert.assertEquals("collection=dummy&profile=_default&s=single+value",
            new PadreQueryStringBuilder(qs, true).buildQueryString());
    }

    @Test
    public void testMultipleSystemParameters() {
        SearchQuestion qs = new SearchQuestion();
        qs.setCollection(new Collection("dummy", null));
        qs.setQuery(null);
        qs.getRawInputParameters().put("s", new String[] {"first value", "second value"});
        
        Assert.assertEquals("collection=dummy&profile=_default&s=first+value+second+value",
            new PadreQueryStringBuilder(qs, true).buildQueryString());
    }
    
    @Test
    public void testSystemQueryWithExistingSystemQuery() {
        SearchQuestion qs = new SearchQuestion();
        qs.setCollection(new Collection("dummy", null));
        qs.setQuery("user entered query");
        qs.getSystemMetaParameters().add("additional");
        qs.getInputParameterMap().put("s", "already present");
        
        Assert.assertEquals("collection=dummy&profile=_default&query=user+entered+query&s=additional+already+present",
                new PadreQueryStringBuilder(qs, true).buildQueryString());
        
    }
    
    @Test
    public void test() {
        PadreQueryStringBuilder builder = new PadreQueryStringBuilder(q, false);
        Assert.assertEquals("a=1&collection=dummy&profile=_default&query=chocolate", builder.buildQueryString());
    }
    
    @Test
    public void testEncoding() {
        q.getAdditionalParameters().put("encoded", new String[] {"a nice & encoded + string"});

        Assert.assertEquals(
                "a=1&collection=dummy&encoded=a+nice+%26+encoded+%2B+string&profile=_default&query=chocolate",
                new PadreQueryStringBuilder(q, false).buildQueryString());
    }

    @Test
    public void testEnvironmentVariables() {
        q.getEnvironmentVariables().put("ABC", "DEF");
        q.getEnvironmentVariables().put("123", "456");
        q.getAdditionalParameters().put("123", new String[] {"456"});
        
        Assert.assertFalse(new PadreQueryStringBuilder(q, false).buildQueryString().contains("ABC"));
        Assert.assertFalse(new PadreQueryStringBuilder(q, false).buildQueryString().contains("123"));
    }
    
    @Test
    public void testMetaParameters() {
        q.getMetaParameters().add("really");
        q.getMetaParameters().add("rules");
        q.getSystemMetaParameters().add("of");
        q.getSystemMetaParameters().add("course");
        
        Assert.assertEquals(
                "a=1&collection=dummy&profile=_default&query=chocolate+really+rules&s=of+course",
                new PadreQueryStringBuilder(q, false).buildQueryString());
    }

    @Test
    public void testFacetQueryConstraints() {
        q.getFacetsQueryConstraints().add("or");
        q.getFacetsQueryConstraints().add("coffee");

        Assert.assertEquals(
                "a=1&collection=dummy&profile=_default&query=chocolate",
                new PadreQueryStringBuilder(q, false).buildQueryString());

        Assert.assertEquals(
                "a=1&collection=dummy&profile=_default&query=chocolate&s=or+coffee",
                new PadreQueryStringBuilder(q, true).buildQueryString());

    }
    
    @Test
    public void testFacetCliveConstraints() {
        when(config.getCollectionType()).thenReturn(Type.meta);
        q.getCollection().setMetaComponents(new String[]{"foo", "bar"});
        
        q.setFacetCollectionConstraints(Optional.of(Arrays.asList("foo", "bar")));
        
        Assert.assertEquals("a=1&collection=dummy&profile=_default&query=chocolate", 
            new PadreQueryStringBuilder(q, false).buildQueryString());
        
        Assert.assertEquals("a=1&clive=bar&clive=foo&collection=dummy&profile=_default&query=chocolate", 
            new PadreQueryStringBuilder(q, true).buildQueryString());
    }
    
    
    @Test
    public void testFacetCliveConstraintsWithOtherCliveValuesSet() {
        when(config.getCollectionType()).thenReturn(Type.meta);
        q.getCollection().setMetaComponents(new String[]{"foo", "bar", "other0", "other1", "fudge"});
        
        q.setFacetCollectionConstraints(Optional.of(Arrays.asList("foo", "bar", "other0", "other1")));
        
        // If the user has set some clive values or something else has we will ensure that
        // the set of live collections is a subset of those collections.
        
        q.getAdditionalParameters().put("clive", new String[]{"foo"});
        
        q.getRawInputParameters().put("clive", new String[]{"bar", "fudge"});
        
        q.getEnvironmentVariables().put("clive", "");
        
        Assert.assertEquals("a=1&clive=foo&collection=dummy&profile=_default&query=chocolate", 
            new PadreQueryStringBuilder(q, false).buildQueryString());
        
        Assert.assertEquals("We should take the intersect of the facet wanted collections and the "
            + "collections the user wanted.", 
            "a=1&clive=bar&clive=foo&collection=dummy&profile=_default&query=chocolate", 
            new PadreQueryStringBuilder(q, true).buildQueryString());
    }
    
    @Test
    public void testFacetCliveConstraintsNoMatchingCollections() {
        when(config.getCollectionType()).thenReturn(Type.meta);
        q.getCollection().setMetaComponents(new String[]{"foo", "bar", "other0", "other1", "fudge"});
        q.setFacetCollectionConstraints(Optional.of(Arrays.asList("other0", "other1")));
        
        // In this case the user or some other input processor wants foo, bar and fudge
        // but facets wants non of those and wants instead other0 and other1 so in this
        // case no collection matches.
        // We must force a zero result page in thise case.
        
        q.getAdditionalParameters().put("clive", new String[]{"foo"});
        
        q.getRawInputParameters().put("clive", new String[]{"bar", "fudge"});
        
        Assert.assertEquals("a=1&clive=foo&collection=dummy&profile=_default&query=chocolate", 
            new PadreQueryStringBuilder(q, false).buildQueryString());
        
        Assert.assertEquals("We should take the intersect of the facet wanted collections and the "
            + "collections the user wanted.", 
            "a=1&collection=dummy&profile=_default&query=chocolate"
            + "&s=+%7CFunDoesNotExist%3Asearchdisabled+%7CFunDoesNotExist%3AnoCollsLive+", 
            new PadreQueryStringBuilder(q, true).buildQueryString());
    }
    
    @Test
    public void testFacetCliveNotAllCollectionsAreKnown() {
        when(config.getCollectionType()).thenReturn(Type.meta);
        // only foo is known bar is unknown
        q.getCollection().setMetaComponents(new String[]{"foo"});
        
        q.setFacetCollectionConstraints(Optional.of(Arrays.asList("foo", "bar")));
        
        Assert.assertEquals("a=1&collection=dummy&profile=_default&query=chocolate", 
            new PadreQueryStringBuilder(q, false).buildQueryString());
        
        Assert.assertEquals("a=1&clive=foo&collection=dummy&profile=_default&query=chocolate", 
            new PadreQueryStringBuilder(q, true).buildQueryString());
    }
    
    @Test
    public void testFacetCliveNotAllCollectionsAreKnownNonMeta() {
        when(config.getCollectionType()).thenReturn(Type.push);
        when(config.getCollectionName()).thenReturn("bar");
        q.getCollection().setMetaComponents(new String[]{});
        
        q.setFacetCollectionConstraints(Optional.of(Arrays.asList("foo", "bar")));
        
        Assert.assertEquals("a=1&collection=dummy&profile=_default&query=chocolate", 
            new PadreQueryStringBuilder(q, false).buildQueryString());
        
        Assert.assertEquals("a=1&clive=bar&collection=dummy&profile=_default&query=chocolate", 
            new PadreQueryStringBuilder(q, true).buildQueryString());
    }
    
    @Test
    public void testFacetCliveNoWantedCollectionsAreKnown() {
        when(config.getCollectionType()).thenReturn(Type.meta);
        // meta collection has only component 'a'
        q.getCollection().setMetaComponents(new String[]{"a"});
        
        q.setFacetCollectionConstraints(Optional.of(Arrays.asList("foo", "bar")));
        
        Assert.assertEquals("a=1&collection=dummy&profile=_default&query=chocolate", 
            new PadreQueryStringBuilder(q, false).buildQueryString());
        
        Assert.assertEquals("a=1&collection=dummy&profile=_default&query=chocolate"
            + "&s=+%7CFunDoesNotExist%3Asearchdisabled+%7CFunDoesNotExist%3AnoCollsLive+", 
            new PadreQueryStringBuilder(q, true).buildQueryString());
    }
    
    @Test
    public void testFacetGScopeConstraints() {
        q.setFacetsGScopeConstraints("1,2+");
        
        Assert.assertEquals(
                "a=1&collection=dummy&profile=_default&query=chocolate",
                new PadreQueryStringBuilder(q, false).buildQueryString());

        Assert.assertEquals(
                "a=1&collection=dummy&gscope1=1%2C2%2B&profile=_default&query=chocolate",
                new PadreQueryStringBuilder(q, true).buildQueryString());
        
        q.getAdditionalParameters().put("gscope1", new String[] {"4,5+"});

        Assert.assertEquals(
                "a=1&collection=dummy&gscope1=1%2C2%2B4%2C5%2B%2B&profile=_default&query=chocolate",
                new PadreQueryStringBuilder(q, true).buildQueryString());

        q.getAdditionalParameters().put("gscope1", new String[] {"6"});

        Assert.assertEquals(
                "a=1&collection=dummy&gscope1=1%2C2%2B6%2B&profile=_default&query=chocolate",
                new PadreQueryStringBuilder(q, true).buildQueryString());
        
        q.setFacetsGScopeConstraints(null);

        Assert.assertEquals(
                "a=1&collection=dummy&gscope1=6&profile=_default&query=chocolate",
                new PadreQueryStringBuilder(q, true).buildQueryString());        
    }
    
    @Test
    public void testMultiValues() {
        q.setQuery("multi");
        q.getAdditionalParameters().put("scope", new String[] {"ab", "cd"});
        
        Assert.assertEquals(
                "a=1&collection=dummy&profile=_default&query=multi&scope=ab&scope=cd",
                new PadreQueryStringBuilder(q, true).buildQueryString());
    }
    
}
