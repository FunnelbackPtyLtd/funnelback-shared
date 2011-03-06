package com.funnelback.publicui.search.model.padre;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * A tier bar
 */
@RequiredArgsConstructor
public class TierBar implements ResultType {

	@Getter private final int matched;
	@Getter private final int outOf;
	
	@Getter @Setter private int firstRank;
	@Getter @Setter private int lastRank;
	
	public final class Schema {
		public static final String TIER_BAR = "tier_bar";
		
		public static final String MATCHED = "matched";
		public static final String OUTOF = "outof";
	}
	
}
