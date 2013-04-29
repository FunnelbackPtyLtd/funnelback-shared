package com.funnelback.publicui.search.model.transaction.session;

import java.net.URI;
import java.net.URL;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A single click on a result
 * 
 * @since 12.5
 */
@Entity
@ToString
public class ClickHistory {
    
    /**
     * Internal database id
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * User who clicked on the result
     */
    @ManyToOne
    @JoinColumn(name="userId")
    @Getter @Setter
    private SearchUser user;

    /** Collection identifier for this click event */
    @Getter @Setter private String collection;

    /** Date when the click was performed */
    @Getter @Setter
    private Date clickDate;

    /** URI of the result in the index */
    @Getter @Setter
    private URI indexUrl;
    
    /** URL to access the result */
    @Getter @Setter
    private URL liveUrl;
    
    /** Title of the result */
    @Getter @Setter
    private String title;
    
    /**
     * Summary of the results biased towards the query
     * that generated this result
     */
    @Getter @Setter
    private String summary;

}
