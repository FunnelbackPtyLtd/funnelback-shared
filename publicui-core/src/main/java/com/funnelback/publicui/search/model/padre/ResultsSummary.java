package com.funnelback.publicui.search.model.padre;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Search result summary, with document counts.
 */
@RequiredArgsConstructor
public class ResultsSummary {
	
	@Getter private final Integer fullyMatching;
	@Getter private final Integer estimatedHits;
	@Getter private final Integer partiallyMatching;
	@Getter private final Integer totalMatching;
	
	@Getter private final Integer numRanks;
	@Getter private final Integer currStart;
	@Getter private final Integer currEnd;
	@Getter private final Integer prevStart;
	@Getter private final Integer nextStart;

	/**
	 * Represents XML Schema
	 *
	 */
	public static final class Schema {
		
		public static final String RESULTS_SUMMARY = "results_summary";
		
		public static final String FULLY_MATCHING = "fully_matching";
		public static final String ESTIMATED_HITS = "estimated_hits";
		public static final String PARTIALLY_MATCHING = "partially_matching";
		public static final String TOTAL_MATCHING = "total_matching";
		public static final String NUM_RANKS = "num_ranks";
		public static final String CURRSTART = "currstart";
		public static final String CURREND = "currend";
		public static final String PREVSTART = "prevstart";
		public static final String NEXTSTART = "nextstart";
	}
	
}