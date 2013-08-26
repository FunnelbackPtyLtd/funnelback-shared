package com.funnelback.publicui.search.model.transaction.session;

import java.net.URI;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

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
    private Long id;

    @NonNull
    @Getter @Setter
    private String collection;

    /**
     * ID of the user who clicked on the result
     */
    @Getter @Setter
    private String userId;

    /** URI of the result in the index */
    @NonNull
    private String indexUrl;

    /**
     * @return URI of the result in the index
     */
    @Transient
    public URI getIndexUrl() {
        return URI.create(indexUrl);
    }

    /**
     * @param uri Sets the URI of the result in the index
     */
    @Transient
    public void setIndexUrl(URI uri) {
        this.indexUrl = uri.toString();
    }
    
    /** Title of the result */
    @Getter @Setter
    @NonNull
    private String title;
    
    /**
     * Summary of the results biased towards the query
     * that generated this result
     */
    @Getter @Setter
    @NonNull
    private String summary;

    
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
