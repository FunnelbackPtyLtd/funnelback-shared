package com.funnelback.publicui.search.model.transaction.testutils;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.funnelback.publicui.search.model.collection.ServiceConfig;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.facet.order.FacetComparatorProvider;

public class TestableSearchTransactionTest {

    @Test
    public void withResultTest() {
        TestableSearchTransaction transaction = new TestableSearchTransaction()
            .withResult(Result.builder().title("a").build())
            .withResult(Result.builder().title("b").build());
        
        Assertions.assertEquals(2, transaction.getResponse().getResultPacket().getResults().size());
        Assertions.assertEquals("a", transaction.getResponse().getResultPacket().getResults().get(0).getTitle());
        Assertions.assertEquals("b", transaction.getResponse().getResultPacket().getResults().get(1).getTitle());
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
        Assertions.assertEquals("b", config.get("a"));
        Assertions.assertEquals("bb", config.get("b"));
        Assertions.assertNull(config.get("c"));
    }
    
    @Test
    public void withFacetAndValuesTest() {
        SearchTransaction transaction = new TestableSearchTransaction()
            .withFacetAndValues(new Facet("authors"))
            .withFacetAndValues(new Facet("authors"), 
                CategoryValue.builder().label("Bob").count(12).selected(false).build(),
                CategoryValue.builder().label("Alice").count(12).selected(true).build());
        
        Assertions.assertEquals(1, transaction.getResponse().getFacets().size());
        Assertions.assertNotNull(transaction.getResponse().getFacetByName("authors"));
        
        Facet f = transaction.getResponse().getFacetByName("authors");
        
        Assertions.assertEquals(2, f.getAllValues().size());
        Assertions.assertEquals(1, f.getSelectedValues().size());
        
        Collections.sort(f.getAllValues(), 
            new FacetComparatorProvider().getComparatorWhenSortingAllValues(f.getOrder(), Optional.ofNullable(f.getCustomComparator())));
        
        Assertions.assertEquals("Bob", f.getAllValues().get(1).getLabel(), "Note the default sorting");
        Assertions.assertEquals("Alice", f.getAllValues().get(0).getLabel(), "Note the default sorting");
    }
    
    @Test
    public void withModificationTest() {
        SearchTransaction transaction = new TestableSearchTransaction()
            .withModification(t -> t.getCustomData().put("a", "b"));
        
        Assertions.assertEquals("b", transaction.getCustomData().get("a"));
    }
    
    @Test
    public void accessDefaultConfigTest() {
        SearchTransaction transaction = new TestableSearchTransaction();
        
        Assertions.assertNull(transaction.getQuestion().getCurrentProfileConfig().get("hello"));
    }
}