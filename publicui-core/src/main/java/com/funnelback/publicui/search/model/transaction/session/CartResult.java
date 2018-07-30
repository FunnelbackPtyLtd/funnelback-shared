package com.funnelback.publicui.search.model.transaction.session;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import java.net.URI;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.PrePersist;

import com.funnelback.publicui.search.model.padre.Result;

import lombok.Getter;
import lombok.Setter;

/**
 * A result in a results cart
 * 
 * @since 12.5
 */
@Entity
public class CartResult extends SessionResult {

    /** Date when the result was added to the cart */
    @Getter @Setter
    private Date addedDate;
    
    /**
     * The name of the collection the indexUrl came from.
     */
    // probably should be on the parent but I am doing the minimum
    @Getter @Setter
    private String collectionForIndexUrl;
    
    /**
     * Metadata values for the result
     */
    @Getter
    @ElementCollection
    @MapKeyColumn(name = "key")
    @CollectionTable(name="CartResultMetadata", joinColumns = {
        @JoinColumn(name="userId", referencedColumnName="userId"),
        @JoinColumn(name="collection", referencedColumnName="collection"),
        @JoinColumn(name="indexUrl", referencedColumnName="indexUrl")
        })
    private final Map<String, String> metaData = new HashMap<>();
    
    /**
     * Builds a {@link CartResult} from a {@link Result}
     * @param r {@link Result} to build from
     * @return A {@link CartResult}
     */
    public static CartResult fromResult(Result r) {
        CartResult cr = new CartResult();
        cr.setCollectionForIndexUrl(r.getCollection());
        cr.setIndexUrl(URI.create(r.getIndexUrl()));
        cr.setTitle(r.getTitle());
        cr.setSummary(r.getSummary());
        cr.getMetaData().putAll(r.getMetaData());
        
        return cr;
    }

    /**
     * Truncate metadata to maximum size allowed in the database
     * before saving to database
     */
    @PrePersist
    protected void prePersist() {
        super.prePersist();
        
        for (Map.Entry<String, String> entry: metaData.entrySet()) {
            if (entry.getValue() != null && entry.getValue().length() > MAX_LEN_METADATA) {
                entry.setValue(entry.getValue().substring(0, MAX_LEN_METADATA-1));
            }
        }
    }
    
}
