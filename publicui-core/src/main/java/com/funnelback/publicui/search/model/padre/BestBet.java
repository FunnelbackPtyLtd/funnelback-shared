package com.funnelback.publicui.search.model.padre;

import java.util.HashMap;
import java.util.Map;

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
	@Getter @Setter private String trigger;
	
	/** Link */
	@Getter @Setter private String link;
	
	/** Title */
	@Getter @Setter private String title;
	
	/** Description */
	@Getter @Setter private String description;
	
	/** URL with click tracking (click.cgi) */
	@Getter @Setter private String clickTrackingUrl;
	
	/**
	 * Custom data place holder for custom processors and
	 * hooks. Anything can be put there by users.
	 */
	@Getter private final Map<String, Object> customData = new HashMap<String, Object>();
	
	public static class Schema {
		public static final String BB = "bb";
		
		public static final String BB_TRIGGER = "bb_trigger";
		public static final String BB_LINK = "bb_link";
		public static final String BB_TITLE = "bb_title";
		public static final String BB_DESC = "bb_desc";
		
		
	}
	
}
