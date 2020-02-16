package com.funnelback.publicui.search.model.log;

import java.net.URI;
import java.net.URL;
import java.util.Date;

import lombok.Getter;
import lombok.ToString;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;

/**
 * A cart click log, for adding/removing/processing cart links
 */
@ToString
public class CartClickLog extends Log {

    /** Type of car event */
    public static enum Type {
        /** Add to cart */
        ADD_TO_CART,
        /** Remove from Cart */
        REMOVE_FROM_CART,
        /** Clear links in cart */
        CLEAR_CART,
        /** Process links in cart */
        PROCESS_CART;
    }

    @Getter final private URL referer;
    @Getter final private URI target;
    @Getter final private Type type;

    /**
     * @param date Date of the event
     * @param collection Collection
     * @param profile Profile
     * @param requestId Request identifier (IP, hash, '-')
     * @param referer URL of the search page where the click is coming from
     * @param target URL of the result (should be the one stored in the index)
     * @param type Type of click
     * @param userId User identifier, may be null
     */
    public CartClickLog(Date date, Collection collection, Profile profile, String requestId,
            URL referer, URI target, Type type, String userId) {
        super(date, collection, profile, requestId, userId);
        this.referer = referer;
        this.target = target;
        this.type = type;
    }
}
