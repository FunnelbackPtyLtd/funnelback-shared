package com.funnelback.publicui.search.model.transaction.session;

import java.net.URI;
import java.net.URL;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

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
    
    /**
     * Internal database id
     */
    @Id
    @GeneratedValue
    protected Long id;

    /**
     * User who clicked on the result
     */
    @ManyToOne
    @JoinColumn(name="userId")
    @Getter @Setter
    protected SearchUser user;

    /** Collection identifier for this click event */
    @Getter @Setter protected String collection;

    /** URI of the result in the index */
    @Getter @Setter
    @NonNull
    protected URI indexUrl;
    
    /** URL to access the result */
    @Getter @Setter
    @NonNull
    protected URL liveUrl;
    
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

}
