package com.funnelback.publicui.search.model.log;

import java.util.Date;

import lombok.Getter;

import org.apache.commons.lang.time.FastDateFormat;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;

/**
 * Log written in the public ui warning file
 *
 */
public class PublicUIWarningLog extends Log {

    /** Date format used in the logs */
    public static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyyMMdd HH:mm:ss");
    
    @Getter private final String message;
    
    /**
     * @param date Date of the warning
     * @param collection Collection
     * @param profile Profile
     * @param requestId Request identifier (IP, hash, '-')
     * @param message Warning message
     */
    public PublicUIWarningLog(Date date, Collection collection, Profile profile, String requestId,
            String message) {
        super(date, collection, profile, requestId);
        this.message = message;
    }
    
    @Override
    public String toString() {
        return DATE_FORMAT.format(date) + " " + collection.getId() + " - " + message;
    }

}
