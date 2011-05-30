package com.funnelback.publicui.search.model.padre;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Search result summary, with document counts.
 */
@AllArgsConstructor
public class ResultsSummary {
	
	@Getter @Setter private Integer fullyMatching;
	@Getter @Setter private Integer estimatedHits;
	@Getter @Setter private Integer partiallyMatching;
	@Getter @Setter private Integer totalMatching;
	
	@Getter @Setter private Integer numRanks;
	@Getter @Setter private Integer currStart;
	@Getter @Setter private Integer currEnd;
	@Getter @Setter private Integer prevStart;
	@Getter @Setter private Integer nextStart;

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