package com.funnelback.publicui.search.model.padre;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.Transformer;

import lombok.Getter;
import lombok.Setter;

/**
 * A PADRE result packet, containing search results.
 */
public class ResultPacket {

	@Getter @Setter private Details details;
	@Getter @Setter private String query;
	@Getter @Setter private String queryAsProcessed;
	@Getter @Setter private String queryCleaned;
	@Getter @Setter private String collection;
	
	@Getter @Setter private ResultsSummary resultsSummary;
	@Getter @Setter private Spell spell;
	@Getter final private List<BestBet> bestBets = new ArrayList<BestBet>();
	@Getter final private List<Result> results = new ArrayList<Result>();
	@Getter final private List<TierBar> tierBars = new ArrayList<TierBar>();
	
	@Getter @Setter private Error error;
	
	/** In ms */
	@Getter @Setter private Integer padreElapsedTime;
	
	/** In seconds */
	@Getter @Setter private Float phlusterElapsedTime;
	
	/**
	 * Indicates how the query was processed.
	 * @see log_codes() in queries/cgi.c
	 */
	@Getter @Setter private String queryProcessorCodes;
	
	@Getter @Setter private ContextualNavigation contextualNavigation;
	
	/**
	 * Metadata counts (Faceting)
	 */
	@Getter private final Map<String, Integer> rmcs = new HashMap<String, Integer>();
	
	/**
	 * URL counts (Faceting)
	 */
	@Getter private final Map<String, Integer> urlCounts = new HashMap<String, Integer>();

	/**
	 * GScope counts (Faceting)
	 */
	@Getter private final Map<Integer, Integer> gScopeCounts = new HashMap<Integer, Integer>();
	
	/**
	 * Scopes (URL prefix, not gscope) included via the -scope parameter
	 */
	@Getter private final List<String> includeScopes = new ArrayList<String>();
	
	/**
	 * Scopes (URL prefix, not gscope) excluded via the -scope parameter
	 */
	@Getter private final List<String> excludeScopes = new ArrayList<String>();
	
	public boolean hasResults() { return results != null && results.size() > 0; }
	
	@SuppressWarnings("unchecked")
	public List<ResultType> getResultsWithTierBars() {
		if (tierBars.size() > 0) {
			ArrayList<ResultType> out = new ArrayList<ResultType>();
			for (TierBar tb: getTierBars()) {
				out.add(tb);
				for (Result r: getResults().subList(tb.getFirstRank(), tb.getLastRank())) {
					out.add(r);
				}
			}
			return out;			
		} else {
			return ListUtils.transformedList(getResults(), new Transformer() {
				@Override
				public Object transform(Object o) {
					return (ResultType) o;
				}
			});
		}
	}
	
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
		
		public static final String PADRE_RESULT_PACKET = "PADRE_result_packet";
		
		public static final String BEST_BETS = "best_bets";
		public static final String RESULTS = "results";
		
		public static final String QUERY = "query";
		public static final String QUERY_AS_PROCESSED = "query_as_processed";
		public static final String COLLECTION = "collection";
		
		public static final String RMC = "rmc";
		public static final String RMC_ITEM = "item";
		
		public static final String URLCOUNT = "urlcount";
		public static final String URLCOUNT_ITEM = "item";
		
		public static final String GSCOPE_COUNTS = "gscope_counts";
		public static final String GSCOPE_MATCHING = "gscope_matching";
		public static final String GSCOPE_VALUE = "value";
		
		public static final String PADRE_ELAPSED_TIME = "padre_elapsed_time";
		public static final String QUERY_PROCESSOR_CODES = "query_processor_codes";
		public static final String PHLUSTER_ELAPSED_TIME = "phluster_elapsed_time";
		
		public static final String INCLUDE_SCOPE = "include_scope";
		public static final String EXCLUDE_SCOPE = "exclude_scope";
		public static final String SCOPE_SEPARATOR = "@";
		
	}
}
