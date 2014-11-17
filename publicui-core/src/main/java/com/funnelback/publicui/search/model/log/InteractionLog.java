package com.funnelback.publicui.search.model.log;

import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.time.FastDateFormat;

import lombok.Getter;
import lombok.ToString;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;

/**
 * User interaction (query completion) log
 */
@ToString
public class InteractionLog extends Log {

    /** Date format used in the logs */
    public static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("EEE MMM d HH:mm:ss yyyy");
    
    /** Type of log, unconstrained to permit future types */
    @Getter private final String logType;
    
    /** URL of the search page where the interaction occurred */
    @Getter private final URL referer;
    
    /** Log event parameters */
    @Getter private final Map<String, String[]> parameters;

    /**
     * @param date Date of the event
     * @param collection Collection
     * @param profile Profile
     * @param requestId Request identifier (IP, hash, '-')
     * @param logType Type of log
     * @param referer URL of the search page
     * @param parameters log event parameters
     * @param userId User identifier, may be null
     */
    public InteractionLog(Date date, Collection collection, Profile profile,
        String requestId, String logType, URL referer, Map<String, String[]> parameters,
        String userId) {
        super(date, collection, profile, requestId, userId);
        this.logType = logType;
        this.referer = referer;
        this.parameters = Collections.unmodifiableMap(parameters);
    }

}
