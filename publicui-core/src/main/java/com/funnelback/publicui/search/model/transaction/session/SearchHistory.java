package com.funnelback.publicui.search.model.transaction.session;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.publicui.utils.URLSignature;

/**
 * A single entry in the {@link SearchUser} search history
 * 
 * @since v12.4
 */
@Entity
@ToString
public class SearchHistory {

    /**
     * Internal database id
     */
    @Id
    @GeneratedValue
    private Long id;
    
    /**
     * ID of the User who performed the search
     */
    @Getter @Setter
    private String userId;
    
    /** Collection identifier for this search event */
    @Getter @Setter private String collection;
    
    /** Date when the search was performed */
    @Getter @Setter private Date searchDate;
    
    /** Original query as entered by the user */
    @Getter @Setter private String originalQuery;
    
    /** Query as processed by the query processor */
    @Getter @Setter private String queryAsProcessed;
    
    /** Total number of results returned */
    @Getter @Setter private int totalMatching;
    
    /** Starting page offset */
    @Getter @Setter private int currStart;
    
    /** Number of results per page */
    @Getter @Setter private int numRanks;
    
    /** URL parameters used to perform the search */
    @Getter private String searchParams;
    
    /**
     * Set the search parameters
     * @param params 
     */
    public void setSearchParams(String params) {
        this.searchParams = params;
        this.searchParamsSignature = URLSignature.computeQueryStringSignature(searchParams);
    }
    
    /**
     * Signature identifying the search URL parameters regardless
     * of the order of the parameters or their encoding
     */
    @Getter private int searchParamsSignature;

}
