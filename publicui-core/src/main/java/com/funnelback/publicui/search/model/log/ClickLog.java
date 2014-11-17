package com.funnelback.publicui.search.model.log;

import java.net.URI;
import java.net.URL;
import java.util.Date;

import lombok.Getter;
import lombok.ToString;

import org.apache.commons.lang3.time.FastDateFormat;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;

/**
 * A click log, for search results or best bets
 * (ex featured pages).
 */
@ToString
public class ClickLog extends Log {

    /** Type of click */
    public static enum Type {
        /** Best bet (featured pages) click */
        FP,
        /** Result click */
        CLICK;
    }
    
    /** Date format used in the logs */
    public static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("EEE MMM d HH:mm:ss yyyy");
    
    @Getter final private URL referer;
    @Getter final private int rank;
    @Getter final private URI target;
    @Getter final private Type type;
    
    /**
     * @param date Date of the event
     * @param collection Collection
     * @param profile Profile
     * @param requestId Request identifier (IP, hash, '-')
     * @param referer URL of the search page where the click is coming from
     * @param rank Rank of the clicked result
     * @param target URL of the result (should be the one stored in the index)
     * @param type Type of click
     * @param userId User identifier, may be null
     */
    public ClickLog(Date date, Collection collection, Profile profile, String requestId,
            URL referer, int rank, URI target, Type type, String userId) {
        super(date, collection, profile, requestId, userId);
        this.referer = referer;
        this.rank = rank;
        this.target = target;
        this.type = type;
    }
}
