package com.funnelback.publicui.search.model.padre;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Search result summary, with document counts and
 * information about the current page returned.
 * 
 * @since 11.0
 */
@AllArgsConstructor
@NoArgsConstructor
public class ResultsSummary {
    
    /** Number of documents that fully matched the query terms. */
    @Getter @Setter private Integer fullyMatching;
    
    /** Number of collapsed results */
    @Getter @Setter private Integer collapsed;
        
    /** Number of documents that partially matched the query terms. */
    @Getter @Setter private Integer partiallyMatching;
    
    /**
     * <p>Total number of documents matching the query terms,
     * fully or partially.</p>
     * 
     * <p>Should be {@link #fullyMatching} + {@link #partiallyMatching}.
     */
    @Getter @Setter private Integer totalMatching;

    /**
     * <p>Whether or not the counts within this summary have been estimated.</p>
     *  
     * <p>Estimation occurs when the result set is only partially scanned (e.g.
     *  more results are found than the daat value).</p>
     */
    @Getter @Setter private Boolean estimatedCounts;

    /**
     * <p>Number of events continued from the day when using event search.</p>
     * 
     * <p>See the <tt>-events</tt> query processor options.</p>
     */
    @Getter @Setter private Integer carriedOverFtd;
    
    /**
     * <p>Number of distinct URLs when using event search.</p>
     * 
     * <p>See the <tt>-events</tt> query processor options.</p>
     */
    @Getter @Setter private Integer totalDistinctMatchingUrls;
    
    /** Number of results returned. */
    @Getter @Setter private Integer numRanks;
    
    /**
     * <p>Current offset of the first result returned.</p>
     * 
     * <p>For example if the second page of results were returned
     * and 10 results were requested, this will be set to 11.</p>
     * 
     * <p>See: <code>start_rank</code> parameter, <code>num_ranks</code> parameter.</p>
     */
    @Getter @Setter private Integer currStart;
    
    /**
     * <p>Current offset of the last result returned.</p>
     * 
     * <p>For example if the second page of results were returned
     * and 10 results were requested, this will be set to 20.</p>
     * 
     * <p>See: <code>start_rank</code> parameter, <code>num_ranks</code> parameter.</p>
     */
    @Getter @Setter private Integer currEnd;
    
    /**
     * <p>Offset of the first result of the previous page of
     * results.</p>
     * 
     * <p>For example if the second page of results were returned
     * and 10 results were requested, this will be set to 1.</p>
     * 
      * <p>See: <code>start_rank</code> parameter, <code>num_ranks</code> parameter.</p>
     */
    @Getter @Setter private Integer prevStart;

    /**
     * <p>Offset of the first result of the next page of
     * results.</p>
     * 
     * <p>For example if the second page of results were returned
     * and 10 results were requested, this will be set to 21.</p>
     * 
      * <p>See: <code>start_rank</code> parameter, <code>num_ranks</code> parameter.</p>
     */
    @Getter @Setter private Integer nextStart;
    
    /** 
     * <p>The number of results which are not viewable to the user</p>
     * 
     * <p>Only set when Translucent DLS is enabled.</p>
     */
    @Getter @Setter private Integer totalSecurityObscuredUrls;
    
    /**
     * Are any URLs promoted (includes URLs that may have been later removed or collapsed).
     * @since 15.12
     */
    @Getter @Setter private boolean anyUrlsPromoted;
    
    /**
     * Was any result afected by result diversification.
     * @since 15.12
     */
    @Getter @Setter private boolean resultDiversificationApplied;

}