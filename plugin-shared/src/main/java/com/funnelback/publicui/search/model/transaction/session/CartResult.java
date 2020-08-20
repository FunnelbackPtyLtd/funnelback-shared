package com.funnelback.publicui.search.model.transaction.session;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * A Cart result presented to the user.
 *
 */
public class CartResult extends SessionResult {

    public CartResult(String userId, 
            String collection, 
            String indexUrl, 
            String title, 
            String summary, 
            Date addedDate,
            Map<String, String> metadata) {
        super(userId, collection, indexUrl, title, summary);
        this.addedDate = addedDate;
        this.metaData = metadata;
    }
    
    public CartResult() {
        this.metaData = new HashMap<>();
    }

    /** Date when the result was added to the cart */
    @Getter @Setter private Date addedDate;
    
    /**
     * Metadata values for the result
     */
    @Getter
    private final Map<String, String> metaData;
    
    /**
     * <p>
     * Since 15.18 this is set to the collection the result came
     * from rather than the collection the search ran on. After 
     * an upgrade from 15.16 or earlier this may still be the meta
     * collection, as the entry in the DB will needed to be updated.
     * </p>
     * 
     * @return the collection for which the result came from it
     * must be a component collection not a meta collection.
     */
    public String getCollection() {
        return super.getCollection();
    }
}
