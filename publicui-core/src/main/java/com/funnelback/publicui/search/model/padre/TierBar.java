package com.funnelback.publicui.search.model.padre;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * <p>A tier bar</p>
 * 
 * <p>Results are grouped by tier bars depending of how
 * they match the query terms. There is usually one
 * tier bar for the fully matching results and anoter one
 * for the partially matching results.<p>
 * 
 * @since 11.0
 */
@RequiredArgsConstructor
public class TierBar implements ResultType {

	/** Number of query terms matched by this tier bar */
	@Getter @Setter private int matched;
	
	/**
	 * Total number of query terms, including the one matched
	 * by this tier bar.
	 */
	@Getter @Setter private int outOf;
	
	/**
	 * Rank of the first search result contained within
	 * this tier bar
	 */
	@Getter @Setter private int firstRank;

	/**
	 * Rank of the last search result contained within
	 * this tier bar
	 */
	@Getter @Setter private int lastRank;
	
	public TierBar(int matched, int outOf) {
		this.matched = matched;
		this.outOf = outOf;
	}

	/** Constants for the PADRE XML result packet tags. */
	public final class Schema {
		public static final String TIER_BAR = "tier_bar";
		
		public static final String MATCHED = "matched";
		public static final String OUTOF = "outof";
	}
	
}
