package com.funnelback.publicui.search.model.transaction.session;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import com.funnelback.publicui.utils.URLSignature;

/**
 * A single entry in the {@link SearchUser} search history
 * 
 * @since v12.4
 */
@Entity
@IdClass(SearchHistoryPK.class)
@ToString
@Log4j2
public class SearchHistory {
    
    public static final int MAX_QUERY_LENGTH = 8196;

    /**
     * ID of the User who performed the search
     */
    @Id
    @Getter @Setter
    private String userId;
    
    /** Collection identifier for this search event */
    @Id
    @Getter @Setter private String collection;

    /**
     * Signature identifying the search URL parameters regardless
     * of the order of the parameters or their encoding
     */
    @Id
    @Getter private int searchParamsSignature;

    /** Date when the search was performed */
    @Getter @Setter private Date searchDate;
    
    /** Original query as entered by the user */
    @Getter private String originalQuery;
    
    public void setOriginalQuery(String q) {
        if(q.length() > MAX_QUERY_LENGTH) {
            //Note that padre actually restricts to MAX_QUERY_LENGTH bytes not chars.
            log.debug("Shrinking original query to {}", MAX_QUERY_LENGTH);
            this.originalQuery = q.substring(0, MAX_QUERY_LENGTH);
        } else {
            this.originalQuery = q;
        }
    }
    
    
    /** Query as processed by the query processor */
    @Getter private String queryAsProcessed;
    
    
    public void setQueryAsProcessed(String q) {
        if(q.length() > MAX_QUERY_LENGTH) {
            //Note that padre actually restricts to MAX_QUERY_LENGTH bytes not chars.
            log.warn("Padre should have truncated the query to {} long it was {} long", MAX_QUERY_LENGTH, q.length());
            this.queryAsProcessed = q.substring(0, MAX_QUERY_LENGTH);
        } else {
            this.queryAsProcessed = q;
        }
    }
    
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
    
}
