package com.funnelback.publicui.search.model.transaction.session;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.PrePersist;

import com.funnelback.publicui.search.model.padre.Result;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A result in a results cart
 * 
 * @since 12.5
 */
@Entity(name=CartResultDBModel.TABLE_NAME)
@NoArgsConstructor
public class CartResultDBModel extends SessionResult {

    public static final String TABLE_NAME = "CartResult";
    
    public CartResultDBModel(String userId, String collection, String indexUrl, String title, String summary,
        Date addedDate, String collectionForIndexUrl, Map<String, String> metadata) {
        super(userId, collection, indexUrl, title, summary);
        this.addedDate = addedDate;
        this.collectionForIndexUrl = collectionForIndexUrl;
        this.metaData.putAll(metadata);
    }

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
     * Builds a {@link CartResultDBModel} from a {@link Result}
     * @param searchedCollection the collection (possible meta collection) on which
     * the search. May not match the collection within r
     * @param r {@link Result} to build from
     * @return A {@link CartResultDBModel}
     */
    public static CartResultDBModel fromResult(String searchedCollection, CartResult cartResult) {
        CartResultDBModel cr = new CartResultDBModel(cartResult.getUserId(), 
            searchedCollection, // In the DB the searchedCollection becomes the collection.  
            cartResult.getIndexUrl().toASCIIString(), 
            cartResult.getTitle(), 
            cartResult.getSummary(),
            cartResult.getAddedDate(), 
            cartResult.getCollection(),
            cartResult.getMetaData());
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
