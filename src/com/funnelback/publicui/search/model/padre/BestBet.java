package com.funnelback.publicui.search.model.padre;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A best bet
 */
@RequiredArgsConstructor
public class BestBet {

	/** Trigger query / regexp */
	@Getter private final String trigger;
	
	/** Link */
	@Getter private final String link;
	
	/** Title */
	@Getter private final String title;
	
	/** Description */
	@Getter private final String description;
	
	public static class Schema {
		public static final String BB = "bb";
		
		public static final String BB_TRIGGER = "bb_trigger";
		public static final String BB_LINK = "bb_link";
		public static final String BB_TITLE = "bb_title";
		public static final String BB_DESC = "bb_desc";
		
		
	}
	
}
