package com.funnelback.publicui.search.model.transaction.session;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;

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
     * Metadata values for the result
     */
    @Getter
    @ElementCollection
    @MapKeyColumn(name = "key")
    @CollectionTable(name="CartResultMetadata", joinColumns = @JoinColumn(name="cartResultId"))
    private final Map<String, String> metaData = new HashMap<>();
    
    /**
     * Builds a {@link CartResult} from a {@link Result}
     * @param r {@link Result} to build from
     * @return A {@link CartResult}
     */
    public static CartResult fromResult(Result r) {
        CartResult cr = new CartResult();
        cr.setCollection(r.getCollection());
        cr.setIndexUrl(URI.create(r.getIndexUrl()));
        cr.setTitle(r.getTitle());
        cr.setSummary(r.getSummary());
        cr.getMetaData().putAll(r.getMetaData());
        
        return cr;
    }

    
}
