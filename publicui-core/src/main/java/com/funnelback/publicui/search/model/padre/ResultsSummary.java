package com.funnelback.publicui.search.model.padre;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Search result summary, with document counts and
 * information about the current page returned.
 * 
 * @since 11.0
 */
@AllArgsConstructor
public class ResultsSummary {
	
	/** Number of documents that fully matched the query terms. */
	@Getter @Setter private Integer fullyMatching;
	
	/**
	 * <p>Estimated number of hits in <code>daat</code> mode.</p>
	 * 
	 * <p>See: the <code>-daat</code> query processor option.</p>
	 */
	@Getter @Setter private Integer estimatedHits;
	
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

	/** Constants for the PADRE XML result packet tags. */
	public static final class Schema {
		
		public static final String RESULTS_SUMMARY = "results_summary";
		
		public static final String FULLY_MATCHING = "fully_matching";
		public static final String ESTIMATED_HITS = "estimated_hits";
		public static final String PARTIALLY_MATCHING = "partially_matching";
		public static final String TOTAL_MATCHING = "total_matching";
		public static final String CARRIED_OVER_FTD = "carried_over_ftd";
		public static final String TOTAL_DISTINCT_MATCHING_URLS = "total_distinct_matching_urls";
		public static final String NUM_RANKS = "num_ranks";
		public static final String CURRSTART = "currstart";
		public static final String CURREND = "currend";
		public static final String PREVSTART = "prevstart";
		public static final String NEXTSTART = "nextstart";
	}
	
}