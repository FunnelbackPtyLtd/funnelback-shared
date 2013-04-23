package com.funnelback.publicui.search.model.log;

import java.util.Date;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;

/**
 * Log event
 */
@RequiredArgsConstructor
public abstract class Log {

    /** String to use when there's no request id */
    public static final String REQUEST_ID_NOTHING = "-";
    
    /** Date of the event */
    @Getter final protected Date date;
    
    /** Collection where the event occurred */
    @Getter final protected Collection collection;
    
    /** Profile where the event occurred */
    @Getter final protected Profile profile;
    
    /**
     * Request identifier, could be an IP, a hash, or null
     */
    @Getter final protected String requestId;
        
}
