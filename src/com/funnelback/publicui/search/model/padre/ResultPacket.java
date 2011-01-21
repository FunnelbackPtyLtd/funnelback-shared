package com.funnelback.publicui.search.model.padre;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * A PADRE result packet, containing search results.
 */
public class ResultPacket {

	@Getter @Setter private Details details;
	@Getter @Setter private String query;
	@Getter @Setter private String queryAsProcessed;
	
	@Getter @Setter private ResultsSummary resultsSummary;
	@Getter @Setter private Spell spell;
	@Getter @Setter private List<Result> results;
	
	@Getter @Setter private Error error;
	
	@Getter @Setter private Integer padreElapsedTime;
	
	@Getter @Setter private ContextualNavigation contextualNavigation;
	
	/**
	 * Metadata counts (Faceting)
	 */
	@Getter private final Map<String, Integer> rmcs = new HashMap<String, Integer>();
	
	public boolean hasResults() { return results != null; }
	
	/* TODO:
	 * Tier bars
	 * Phluster elapsed time
	 * Include scope
	 * Exclude Scope
	 * Query Processor Codes
	 * ...
	 */
	
	/**
	 * Represents XML Schema
	 *
	 */
	public static final class Schema {
		
		public static final String RESULTS = "results";
		
		public static final String QUERY = "query";
		public static final String QUERY_AS_PROCESSED = "query_as_processed";
		
		public static final String RMC = "rmc";
		public static final String RMC_ITEM = "item";
		
		public static final String PADRE_ELAPSED_TIME = "padre_elapsed_time";
	}
}
