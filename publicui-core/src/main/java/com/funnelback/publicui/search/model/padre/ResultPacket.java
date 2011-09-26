package com.funnelback.publicui.search.model.padre;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.Transformer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * A PADRE result packet, containing search results.
 * 
 * @since 11.0
 */
public class ResultPacket {

	/** Details about the index and PADRE version. */
	@Getter @Setter private Details details;
	
	/** Original query terms */
	@Getter @Setter private String query;
	
	/**
	 * <p>Query terms as processed by PADRE.</p>
	 * 
	 * <p>This could be different from the {@link #query} if the
	 * initial {@link #query} contained character that PADRE ignores
	 * of if a query transformation was applied by PADRE.</p>
	 */
	@Getter @Setter private String queryAsProcessed;
	
	/**
	 * The query, cleaned from any operator or constraint that was
	 * automatically added by the faceted navigation system.
	 */
	@Getter @Setter private String queryCleaned;
	
	/** ID of the collection being searched. */
	@Getter @Setter private String collection;

	/**
	 * List of additional queries for the Query Blending system.
	 * 
	 * @see <code>blending.cfg</code>
	 */
	@Getter private final List<QSup> qSups = new ArrayList<QSup>();
	
	/**
	 * Summary counts and data about the results (How many documents
	 * matched, which page is currently returned, etc.).
	 */
	@Getter @Setter private ResultsSummary resultsSummary;
	
	/** Spelling suggestions. */
	@Getter @Setter private Spell spell;
	
	/** List of best bets matching the query. */
	@Getter final private List<BestBet> bestBets = new ArrayList<BestBet>();
	
	/** List of results. */
	@Getter final private List<Result> results = new ArrayList<Result>();
	
	/** List of tier bars */
	@Getter final private List<TierBar> tierBars = new ArrayList<TierBar>();
	
	/** Error occured during the search, if any. */
	@Getter @Setter private Error error;
	
	/** Time spent by PADRE processing the query, in milliseconds */
	@Getter @Setter private Integer padreElapsedTime;
	
	/**
	 * Time spent by PADRE processing contextual navigation,
	 * in seconds.
	 */
	@Getter @Setter private Float phlusterElapsedTime;
	
	/**
	 * Indicates how the query was internally processed
	 * by PADRE.
	 */
	@Getter @Setter private String queryProcessorCodes;

	/** Contextual navigation suggestions. */
	@Getter @Setter private ContextualNavigation contextualNavigation;
	
	/**
	 * <p>Metadata counts (Used in faceted navigation).</p>
	 * 
	 * <p>The key is the couple of <code>class:value</code> and the
	 * value is the count.</p>
	 * 
	 * <p>
	 * 	Examples:
	 *  <ul>
	 *  	<li>a:mozart => 12</li>
	 *  	<li>a:beethoven => 6</li>
	 *  </ul>
	 * </p>
	 */
	@Getter private final Map<String, Integer> rmcs = new HashMap<String, Integer>();
	
	/**
	 * <p>URL counts (Used in faceted navigation).</p>
	 * 
	 * <p>The key is the URL itself and the value is the count.
	 * If the URL starts with <code>http://</code>, it's omitted.</p>
	 * 
	 * <p>
	 * Examples:
	 * 	<ul>
	 * 		<li>www.example.com/about => 12</li>
	 * 		<li>www.example.com/contact => 6 </li>
	 *  	<li>https://secure.example.com/login => 5</li>
	 *  </ul>
	 * </p>
	 */
	@Getter private final Map<String, Integer> urlCounts = new HashMap<String, Integer>();

	/**
	 * <p>GScope counts (Used in faceted navigation)</p>
	 * 
	 * <p>The key is the GScope number and the value is the count.</p>
	 */
	@Getter private final Map<Integer, Integer> gScopeCounts = new HashMap<Integer, Integer>();
	
	/**
	 * <p>Regular expression to use to highlight query terms in titles,
	 * summaries or metadata.</p>
	 * 
	 * <p>PADRE provides the regular expression to use depending on the
	 * query terms and other factors.</p>
	 */
	@Getter @Setter private String queryHighlightRegex;
	
	/**
	 * <p>Origin of the search, for geographical searches.</p>
	 * 
	 * <p>The first slot contains the latitude, the second slot
	 * contains the longitude.</p>
	 */
	@Getter @Setter private Float[] origin = new Float[0]; 
	
	/**
	 * <p>List of prominent entities.</p>
	 * 
	 * <p>See the experimental <tt>-fluent</tt> query processor option.</p>
	 */
	@Getter private final Map<String, Integer> entityList = new HashMap<String, Integer>();
	
	/**
	 * Scopes (URL prefixes, not Gscope) included via the <code>scope</code> 
	 * query processor option.
	 */
	@Getter private final List<String> includeScopes = new ArrayList<String>();
	
	/**
	 * Scopes (URL prefixes, not Gscopes) excluded via the <code>scope</code>
	 * query processor option.
	 */
	@Getter private final List<String> excludeScopes = new ArrayList<String>();
	
	/**
	 * A {@link Map} of floats that describe the cooler ranking weights. Weights are 
	 * identified by the cooler variable short name, 
	 * and the map is only populated when explain mode is on.
	 */
	@Getter private final Map<String,Float> coolerWeights = new HashMap<String,Float>();

	/**
	 * A {@link Map} of Strings that describes how to calculate the potential improvement for ranking 
	 * on each feature when the content optimiser is used. 
	 * Ranking features are identified by the cooler variable short name, 
	 * and the map is only populated when explain mode is on.
	 */
	@Getter private final Map<String,String> explainTypes = new HashMap<String,String>();
	
	/**
	 * A {@link List} of stop words used by the query processor. Only populated when explain mode is on.
	 */
	@Getter private final List<String> stopWords = new ArrayList<String>();
	
	/**
	 * A {@link SetMultimap} of Strings that describes the results of stemming on the query. Only populated when explain mode is on.
	 * The map keys are content terms, and the value(s) are the query terms that the key matches.   
	 */
	@Getter private SetMultimap<String,String> StemmedEquivs = HashMultimap.create();

	/**
	 * A {@link Map} of long names for cooler ranking variables, keyed by the cooler variable short names. Only populated when explain mode is on.    
	 */
	@Getter private Map<String,String> coolerNames = new HashMap<String,String>();
	
	/**
	 * Test if the packet contains results.
	 * @return true if the packet contains at least one {@link Result}.
	 */
	public boolean hasResults() { return results != null && results.size() > 0; }
	
	/**
	 * <p>Get the results <em>and</em the tier bars mixed together.</p>
	 * 
	 * <p>This is a convenience method if you need to iterate over the result set
	 * and display tier bars.</p>
	 * 
	 * @return A list containing both {@link Result} and {@link TierBar}, in
	 * the order returned by PADRE.
	 */
	@SuppressWarnings("unchecked")
	public List<ResultType> getResultsWithTierBars() {
		try {
			if (tierBars.size() > 0) {
				ArrayList<ResultType> out = new ArrayList<ResultType>();
				for (TierBar tb: getTierBars()) {
					out.add(tb);
					for (Result r: getResults().subList(tb.getFirstRank(), tb.getLastRank()-1)) {
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
		} catch (Throwable t) {
			// Ignore errors
			return new ArrayList<ResultType>();
		}
	}
	
	/** Constants for the PADRE XML result packet tags. */
	public static final class Schema {
		
		public static final String PADRE_RESULT_PACKET = "PADRE_result_packet";
		
		public static final String BEST_BETS = "best_bets";
		public static final String RESULTS = "results";
		
		public static final String QUERY = "query";
		public static final String QUERY_AS_PROCESSED = "query_as_processed";
		public static final String COLLECTION = "collection";
		
		public static final String QSUP = "qsup";
		public static final String QSUP_SRC = "src";
		
		public static final String RMC = "rmc";
		public static final String RMC_ITEM = "item";
		
		public static final String URLCOUNT = "urlcount";
		public static final String URLCOUNT_ITEM = "item";
		
		public static final String GSCOPE_COUNTS = "gscope_counts";
		public static final String GSCOPE_MATCHING = "gscope_matching";
		public static final String GSCOPE_VALUE = "value";
		
		public static final String QHLRE = "qhlre";
		public static final String ORIGIN = "origin";
		
		public static final String PADRE_ELAPSED_TIME = "padre_elapsed_time";
		public static final String QUERY_PROCESSOR_CODES = "query_processor_codes";
		public static final String PHLUSTER_ELAPSED_TIME = "phluster_elapsed_time";
		
		public static final String INCLUDE_SCOPE = "include_scope";
		public static final String EXCLUDE_SCOPE = "exclude_scope";
		public static final String SCOPE_SEPARATOR = "@";
		public static final String COOLER_WEIGHTINGS = "cooler_weightings";

		public static final String ENTITYLIST = "entitylist";
		public static final String ENTITY = "entity";
		public static final String CNT = "cnt";
		
		public static final String EXPLAIN_TYPES = "explain_types";

		public static final String STOP_WORDS = "stop_words";

		public static final String STEM_EQUIV = "stem_equivs";

		public static final String COOLER_NAMES = "cooler_names";
		
	}
}
