package com.funnelback.publicui.search.model.transaction.session;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

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
@IdClass(SearchHistoryPK.class)
@ToString
public class SearchHistory {

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
    
}
