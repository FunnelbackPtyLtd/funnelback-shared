package com.funnelback.publicui.search.model.padre;

import java.util.Date;
import java.util.Locale;

import java.text.SimpleDateFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>Details (header) of a PADRE result packet</p>
 * 
 * <p>Contains information about the collection and the
 * PADRE version.</p>
 * 
 * @since 11.0
 */
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Details {

    /** Pattern to use to parse the updated date of the index. */
    private static final String UPDATED_DATE_PATTERN = "EEE MMM dd HH:mm:ss yyyy";
    
    public static final String getUpdateDatePatternWithoutLocal() {
        return UPDATED_DATE_PATTERN;
    }
    
    public static final SimpleDateFormat getUpdateDateFormat() {
        return new SimpleDateFormat(UPDATED_DATE_PATTERN, Locale.ENGLISH);
    }

    /** Version of the PADRE query processor. */
    @Getter @Setter private String padreVersion;
    
    /** Size of the index. */
    @Getter @Setter private String collectionSize;
    
    /** Last updated date of the index. */
    @Getter @Setter private Date collectionUpdated;
    
    /** Constants for the PADRE XML result packet tags. */
    public static final class Schema {
        public static final String DETAILS = "details";
        
        public static final String PADRE_VERSION = "padre_version";
        public static final String COLLECTION_SIZE = "collection_size";
        public static final String COLLECTION_UPDATED = "collection_updated";
    }
}
