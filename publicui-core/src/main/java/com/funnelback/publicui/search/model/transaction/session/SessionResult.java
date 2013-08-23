package com.funnelback.publicui.search.model.transaction.session;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Search result base class when used in a session context
 * 
 * @since 12.5
 */
@ToString
@MappedSuperclass
public abstract class SessionResult {
    
    /** Size of the column holding the summary */
    private static final int MAX_LEN_SUMMARY = 1024;
    
    /**
     * Internal database id
     */
    @Id
    @GeneratedValue
    protected Long id;

    /**
     * ID of the user who clicked on the result
     */
    @Getter @Setter
    protected String userId;

    /** Collection identifier for this click event */
    @Getter @Setter protected String collection;

    /** URI of the result in the index */
    @NonNull
    protected String indexUrl;

    @Transient
    public URI getIndexUrl() {
        return URI.create(indexUrl);
    }

    @Transient
    public void setIndexUrl(URI uri) {
        this.indexUrl = uri.toString();
    }
    
    /** Title of the result */
    @Getter @Setter
    @NonNull
    protected String title;
    
    /**
     * Summary of the results biased towards the query
     * that generated this result
     */
    @Getter @Setter
    @NonNull
    protected String summary;

    @Getter
    @ElementCollection
    @MapKeyColumn(name = "key")
    @CollectionTable(name="CartResultMetadata", joinColumns = @JoinColumn(name="cartResultId"))
    protected final Map<String, String> metaData = new HashMap<>();
    
    /**
     * Truncate summary to maximum size allowed in the database
     * before saving to database
     */
    @PrePersist
    private void prePersist() {
        if (summary != null && summary.length() > MAX_LEN_SUMMARY) {
            summary = summary.substring(0, MAX_LEN_SUMMARY-1);
        }
    }

}
