package com.funnelback.publicui.search.model.transaction.testutils;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.collection.ServiceConfig;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;

public class TestableSearchTransactionTest {

    @Test
    public void withResultTest() {
        TestableSearchTransaction transaction = new TestableSearchTransaction()
            .withResult(Result.builder().title("a").build())
            .withResult(Result.builder().title("b").build());
        
        Assert.assertEquals(2, transaction.getResponse().getResultPacket().getResults().size());
        Assert.assertEquals("a", transaction.getResponse().getResultPacket().getResults().get(0).getTitle());
        Assert.assertEquals("b", transaction.getResponse().getResultPacket().getResults().get(1).getTitle());
    }
    
    @Test
    public void withProfileSettingTest() {
        TestableSearchTransaction transaction = new TestableSearchTransaction()
            .withProfileSetting("a", "a")
            .withProfileSetting("a", "b")
            .withProfileSetting("b", "bb")
            .withProfileSetting("c", "c")
            .withProfileSetting("c", null);
        
        ServiceConfig config = transaction.getQuestion().getCurrentProfileConfig();
        Assert.assertEquals("b", config.get("a"));
        Assert.assertEquals("bb", config.get("b"));
        Assert.assertNull(config.get("c"));
    }
    
    @Test
    public void withFacetAndValuesTest() {
        SearchTransaction transaction = new TestableSearchTransaction()
            .withFacetAndValues(new Facet("authors"))
            .withFacetAndValues(new Facet("authors"), 
                CategoryValue.builder().label("Bob").count(12).selected(false).build(),
                CategoryValue.builder().label("Alice").count(12).selected(true).build());
        
        Assert.assertEquals(1, transaction.getResponse().getFacets().size());
        Assert.assertNotNull(transaction.getResponse().getFacetByName("authors"));
        
        Facet f = transaction.getResponse().getFacetByName("authors");
        
        Assert.assertEquals(2, f.getAllValues().size());
        Assert.assertEquals(1, f.getSelectedValues().size());
        
        Assert.assertEquals("Note the default sorting", "Bob", f.getAllValues().get(1).getLabel());
        Assert.assertEquals("Note the default sorting", "Alice", f.getAllValues().get(0).getLabel());
    }
    
    @Test
    public void withModificationTest() {
        SearchTransaction transaction = new TestableSearchTransaction()
            .withModification(t -> t.getCustomData().put("a", "b"));
        
        Assert.assertEquals("b", transaction.getCustomData().get("a"));
    }
}
