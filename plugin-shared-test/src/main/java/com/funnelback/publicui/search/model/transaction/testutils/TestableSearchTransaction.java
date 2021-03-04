package com.funnelback.publicui.search.model.transaction.testutils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.Assert;

import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * A search transaction that makes testing easier.
 * 
 * for example:
 * <pre>{@code
 * SearchTransaction transaction = new TestableSearchTransaction()
 *     .withResult(Result.builder().title("hello").liveUrl("http://example/com/").build())
 *     
 *     // Add a profile setting will be returned by transaction.getQuestion().getCurrentProfileConfig()
 *     .withProfileSetting("a", "b")
 *     
 *     // Add a facet to the result packet.
 *     .withFacetAndValues(new Facet("authors"), 
 *         CategoryValue.builder().label("Bob").count(12).selected(false).build(),
 *         CategoryValue.builder().label("Alice").count(12).selected(true).build())
 *     
 *     // Use the modifier to set the form name in a single line.
 *     .withModification(t -> t.getQuestion().setForm("super"))
 *     
 *     // Use the modifier to add some custom data to the transaction.
 *     .withModification(t -> {
 *         if(t.getCustomData().isEmpty()) {
 *             t.getCustomData().put("a", "b");
 *             t.getCustomData().put("b", "c");
 *         }
 *     });
 * }</pre>
 *
 */
public class TestableSearchTransaction extends SearchTransaction {

    public TestableSearchTransaction() {
        super();
        // Set the profile config to empty by default, since a profile config will always be defined in practice
        SearchQuestionTestHelper.setCurrentProfileConfig(new HashMap<>(), getQuestion());
    }
    
    /**
     * Adds a result to the list of results in the search transaction.
     * 
     * <pre>{@code
     * .withResult(Result.builder().title("hello").liveUrl("http://example/com/1").build())
     * .withResult(Result.builder().title("hi").liveUrl("http://example/com/2").build())
     * }</pre>
     * 
     * @param resultToAdd the result to add to the {@link ResultPacket}, a result packet will
     * be created if it does not already exist.
     * 
     * @return this
     */
    public TestableSearchTransaction withResult(Result resultToAdd) {
        withResultPacketIfNotSet();
        this.getResponse().getResultPacket().getResults().add(resultToAdd);
        return this;
    }
    
    /**
     * Sets profile settings on to the SearchTransaction, these settings will be returned by {@link SearchQuestion#getCurrentProfileConfig()}.
     * 
     * @param key The profile setting key.
     * @param value The value to set the key to, when null the setting will be removed.
     * @return this;
     */
    public TestableSearchTransaction withProfileSetting(String key, String value) {
        SearchQuestionTestHelper.setProfileConfigSetting(getQuestion(), key, value);
        return this;
    }
    
    /**
     * Ensures the SearchTransaction has a result packet, if none is set it will set one.
     * 
     * @return this
     */
    public TestableSearchTransaction withResultPacketIfNotSet() {
        if(this.getResponse().getResultPacket() == null)
            return this.withResultPacket(new ResultPacket());
        return this;
    }
    
    /**
     * Adds the given result packet to the search transaction.
     * 
     * @param resultPacket the result packet to set on the search transaction
     * @return this
     */
    public TestableSearchTransaction withResultPacket(ResultPacket resultPacket) {
        this.getResponse().setResultPacket(resultPacket);
        return this;
    }
    
    /**
     * Adds the given facet to the search transaction, and adds all category values to the facet.
     * <pre>{@code
     * .withFacetAndValues(new Facet("authors"), 
     *              CategoryValue.builder().label("Bob").count(12).selected(false).build(),
     *              CategoryValue.builder().label("Alice").count(12).selected(true).build())
     * }</pre>
     * @param facetToAdd the facet to add to the search transaction, will replace any existing
     * facet with the same name.
     * @param values these are the values from the facet for example for a facet over metadata author
     * this might contain all authors e.g. "Bob". 
     * @return this
     */
    public TestableSearchTransaction withFacetAndValues(Facet facetToAdd,  CategoryValue ...values) {
        return withFacetAndValues(facetToAdd, Optional.ofNullable(values).map(Arrays::asList).orElse(List.of()));
    }
    
    /**
     * Adds the given facet to the search transaction, and adds all category values to the facet.
     * 
     * @param facetToAdd the facet to add to the search transaction, will replace any existing
     * facet with the same name.
     * @param values these are the values from the facet for example for a facet over metadata author
     * this might contain all authors e.g. "Bob". 
     * @return this
     */
    public TestableSearchTransaction withFacetAndValues(Facet facetToAdd,  List<CategoryValue> values) {
        Assert.assertNotNull("Facet names must not be null", facetToAdd.getName());
        // Clear out any existing facets with the same name.
        this.getResponse().getFacets().removeIf(f -> facetToAdd.getName().equals(f.getName()));
        
        facetToAdd.getAllValues().addAll(values);
        
        this.getResponse().getFacets().add(facetToAdd);
        
        return this;
    }
    
    /**
     * Allows for easy chaining of modifications to this SearchTransaction
     * 
     * For example:
     * <pre>{@code
     *     // Use the modifier to set the form name in a single line.
     *     .withModification(t -> t.getQuestion().setForm("super"))
     *     
     *     // Use the modifier to add some custom data to the transaction.
     *     .withModification(t -> {
     *         if(t.getCustomData().isEmpty()) {
     *             t.getCustomData().put("a", "b");
     *             t.getCustomData().put("b", "c");
     *         }
     *     });
     * .with...() // chain more here
     * }</pre>
     * 
     * @param modify
     * @return
     */
    public TestableSearchTransaction withModification(Consumer<TestableSearchTransaction> modify) {
        modify.accept(this);
        return this;
    }
    
}
