package com.funnelback.publicui.search.model.padre;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * A best bet
 */
@RequiredArgsConstructor
@AllArgsConstructor
public class BestBet {

	/** Trigger query / regexp */
	@Getter private final String trigger;
	
	/** Link */
	@Getter private final String link;
	
	/** Title */
	@Getter private final String title;
	
	/** Description */
	@Getter private final String description;
	
	/** URL with click tracking (click.cgi) */
	@Getter @Setter private String clickTrackingUrl;
	
	public static class Schema {
		public static final String BB = "bb";
		
		public static final String BB_TRIGGER = "bb_trigger";
		public static final String BB_LINK = "bb_link";
		public static final String BB_TITLE = "bb_title";
		public static final String BB_DESC = "bb_desc";
		
		
	}
	
}
