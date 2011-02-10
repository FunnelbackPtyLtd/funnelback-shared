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
	
	@Getter @Setter private ResultsSummary resultsSummary;
	@Getter @Setter private Spell spell;
	@Getter final private List<BestBet> bestBets = new ArrayList<BestBet>();
	@Getter final private List<Result> results = new ArrayList<Result>();
	@Getter final private List<TierBar> tierBars = new ArrayList<TierBar>();
	
	@Getter @Setter private Error error;
	
	@Getter @Setter private Integer padreElapsedTime;
	
	@Getter @Setter private ContextualNavigation contextualNavigation;
	
	/**
	 * Metadata counts (Faceting)
	 */
	@Getter private final Map<String, Integer> rmcs = new HashMap<String, Integer>();
	
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
		
		public static final String BEST_BETS = "best_bets";
		public static final String RESULTS = "results";
		
		public static final String QUERY = "query";
		public static final String QUERY_AS_PROCESSED = "query_as_processed";
		
		public static final String RMC = "rmc";
		public static final String RMC_ITEM = "item";
		
		public static final String PADRE_ELAPSED_TIME = "padre_elapsed_time";
	}
}
