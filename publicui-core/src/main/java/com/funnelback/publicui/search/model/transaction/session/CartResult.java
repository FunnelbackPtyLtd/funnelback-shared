package com.funnelback.publicui.search.model.transaction.session;

import com.funnelback.publicui.search.model.padre.Result;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import java.net.URI;
import java.util.Date;

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
     * Creates a {@link SessionResult} from a {@link com.funnelback.publicui.search.model.padre.Result}
     * @param r {@link com.funnelback.publicui.search.model.padre.Result} to clone
     * @return A {@link SessionResult} with copied fields
     */
    public static CartResult fromResult(Result r) {
        CartResult sr = new CartResult();
        sr.setCollection(r.getCollection());
        sr.setIndexUrl(URI.create(r.getIndexUrl()));
        sr.setTitle(r.getTitle());
        sr.setSummary(r.getSummary());
        sr.getMetaData().putAll(r.getMetaData());

        return sr;
    }

}
