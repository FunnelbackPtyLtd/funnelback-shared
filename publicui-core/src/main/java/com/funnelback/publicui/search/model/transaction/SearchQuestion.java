package com.funnelback.publicui.search.model.transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.log.Log;
import com.funnelback.publicui.utils.SingleValueMapWrapper;

/**
 * <p>This class contains all the input data related to a search.</p>
 * 
 * <p>This data will be collected from the input parameters (query string
 * parameters) and possibly transformed by some processing before PADRE
 * is called.</p>
 * 
 * @since 11.0
 */
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SearchQuestion {
		
	/**
	 * <p>Query terms.</p>
	 * 
	 * <p>This will be possibly transformed before being passed to
	 * PADRE, depending of the configuration.</p>
	 */
	@Getter @Setter private String query;
	
	/**
	 * Original query, as entered by the user.
	 */
	@Getter @Setter private String originalQuery;
	
	/**
	 * Searched {@link Collection}.
	 */
	@Getter @Setter private Collection collection;
	
	/**
	 * Search {@link Profile}, defaulting to "_default"
	 */
	@NonNull
	@javax.validation.constraints.Pattern(regexp="[\\w-_]+")
	@Getter @Setter private String profile = DefaultValues.DEFAULT_PROFILE;
	
	/**
	 * Specific component of a meta-collection to query
	 */
	@Getter @Setter private String[] clive;
	
	/**
	 * Display form (template)
	 */
	@NonNull
	@javax.validation.constraints.Pattern(regexp="[\\w-_]+")		
	@Getter @Setter private String form = DefaultValues.DEFAULT_FORM;
	
	/**
	 * Contextual Navigation: last clicked cluster
	 */
	@Getter @Setter private String cnClickedCluster;
	
	/**
	 * Contextual Navigation: Previous clicked clusters
	 */
	@Getter private final List<String> cnPreviousClusters = new ArrayList<String>();
	
	/**
	 * <p>Query expressions, will be passed to PADRE.</p>
	 * 
	 * <p>This list will contain the original query entered by the user
	 * but also any other expression calculated for other reasons (faceted
	 * navigation constraints, etc.).</p>
	 */
	@Getter final private List<String> queryExpressions = new ArrayList<String>();
	
	/**
	 * <tt>meta_*</tt> / <tt>query_*</tt> input parameters, transformed as query expressions.
	 */
	@Getter final private List<String> metaParameters = new ArrayList<String>();
	
	/**
	 * <p>Additional parameters to pass as-is to PADRE.</p>
	 * 
	 * <p>The values of this map will be passed through to PADRE and won't be processed
	 * at all by the Modern UI.</p>
	 * 
	 * <p>This map is not supposed to be changed. If you need to inject or manipulate Modern UI
	 * parameters {@link #inputParameterMap} or {@link #rawInputParameters} is more suitable.</p>
	 * 
	 */
	@Getter final private Map<String, String[]> additionalParameters = new HashMap<String, String[]>();
	
	/**
	 * List of environment variables to pass to PADRE.
	 */
	@Getter final private Map<String, String> environmentVariables = new HashMap<String, String>();

	/**
	 * <p>Dynamic query processor options for PADRE, in addition to the one set in
	 * <tt>collection.cfg</tt>.</p>
	 * 
	 * <p>Will be updated by the input processing.</p>
	 */
	@Getter final private List<String> dynamicQueryProcessorOptions = new ArrayList<String>();

	/**
	 * <p>User keys for early binding Document Level Security.</p>
	 */
	@Getter final private List<String> userKeys = new ArrayList<String>();
	
	/**
	 * <p>List of selected facets.</p>
	 * 
	 * <p>Contains the name of the facets that were selected, such as
	 * "Location" or "Brand", but not the actual value.</p>
	 */
	@Getter final private Set<String> selectedFacets = new HashSet<String>();
	
	/**
	 * <p>List of selected facets categories.</p>
	 * 
	 * <p>Contains the actual values that were selected, indexed by facet.
	 * For example: "Location" => ("Sydney", "Melbourne").</p>
	 */
	@Getter final private Map<String, List<String>> selectedCategoryValues = new HashMap<String, List<String>>();
	
	/**
	 * Query constraints to apply for faceted navigation
	 * (In addition to other query expressions).
	 */
	@Getter @Setter private List<String> facetsQueryConstraints = new ArrayList<String>();
	
	/**
	 * GScope constraints to apply for faceted navigation
	 * (In addition to existing gscope constraints).
	 */
	@Getter @Setter private String facetsGScopeConstraints;
	
	/**
	 * Whether the request is impersonated (Document Level Security)
	 */
	@Getter @Setter private boolean impersonated;
	
	/**
	 * User identifier for this transaction. Depending of the collection configuration
	 * it can be an IP address, an md5 hash of the address, nothing ("-") or null. 
	 */
	@Getter @Setter private String userId = Log.USERID_NOTHING;

	/**
	 * <p>Raw input parameters</p>
	 * 
	 * <p>Contains all the input parameters (query string / request parameters).</p>
	 * 
	 * <p>Be aware that the value type is <code>String array</code>, allowing for multiple
	 * value of the same parameter (e.g. <code>&amp;param=value1&amp;param=value2</code>).
	 * Putting a single valued <code>String</code> in this Map will not work, it must be
	 * an array of size one. Example in a Groovy hook script:
	 * <pre>
	 * transaction.question.rawInputParameters["param"] = [ "value" ]
	 * </pre>
	 * </p>
	 * 
	 * @see {@link inputParameterMap} provides a simpler way to inject or retrieve simple mono-valued
	 * parameters. {@link inputParameterMap} and {@link #rawInputParameters} are backed by the same Map: Any
	 * change made in one will be reflected in the other.
	 */
	@Getter private final Map<String, String[]> rawInputParameters = new HashMap<String, String[]>();

	/**
	 * <p>Input parameters map.</p>
	 * 
	 * <p>Contains all the input parameters (query string / request parameters) in a
	 * convenient fashion: Only the first value of each parameter is returned to avoid
	 * having to deal with arrays of Strings.</p>
	 * 
	 * <p>For example if the query string is <code>&amp;param=value1&amp;param=value2</code> 
	 * then this map will contain only one key-value pair: <tt>param=value1</tt>. The <tt>value2</tt>
	 * won't be available unless you use {@link #rawInputParameters}.</p>
	 * 
	 * @see {@link rawInputParameters} provides a way to inject or retrieve multi-valued parameters. Both
	 * {@link inputParameterMap} and {@link #rawInputParameters} are backed by the same Map: Any change made
	 * in one will be reflected in the other.
	 */
	@Getter private final Map<String, String> inputParameterMap = new SingleValueMapWrapper(rawInputParameters);
	
	/**
	 * <p>Indicates if this question is part of the "main" search, or part of an "extra"
	 * search.</p>
	 * 
	 * <p>This is needed to perform some actions only on the main search but not for every
	 * extra search.</p>
	 */
	@Getter @Setter private boolean extraSearch = false;

	/**
	 * Common query string parameters names.
	 * 
	 * @since 11.0
	 */
	public static class RequestParameters {
		
		/** Collection ID */
		public static final String COLLECTION = "collection";
		
		/** Query terms */
		public static final String QUERY = "query";
		
		/** Search profile */
		public static final String PROFILE = "profile";
		
		/** Meta components restriction*/
		public static final String CLIVE = "clive";
		
		/** Gscope constraint */
		public static final String GSCOPE1 = "gscope1";
		
		/** Number of results per page */
		public static final String NUM_RANKS = "num_ranks";
		
		/** Explore: Number of query terms */
		public static final String EXP = "exp";

		/**
		 * Prefix used on faceted navigation parameters
		 * (Ex: <code>f.Location|X=...</code>
		 */
		public static final String FACET_PREFIX = "f.";
		
		/**
		 * Checkbox used to preserve the facet scope when
		 * running a query.
		 */
		public static final String FACET_SCOPE = "facetScope";
		
		/**
		 * Pattern to use to find faceted navigation parameters.
		 */
		public static final Pattern FACET_PARAM_PATTERN = Pattern.compile("^" + FACET_PREFIX.replaceAll("\\.", "\\\\.") + "([^\\|]+)(\\|(.*))?");
		
		/**
		 * Content Optimiser: URL of the document to optimise.
		 */
		public static final String CONTENT_OPTIMISER_URL = "optimiser_url";
		
		/**
		 * Content Optimiser: Advanced mode toggle.
		 */
		public static final String CONTENT_OPTIMISER_ADVANCED = "advanced";

		/** Content Optimiser: Activate explain mode on PADRE. */
		public static final String EXPLAIN = "explain";		

		/**
		 * Common request header names
		 * 
		 * @since 11.1
		 */
		public static class Header {
			/**
			 * Header used to provide the originating user's IP address, which may differ from
			 * the normal remote address if the request is forwarded by a proxy or wrapped by
			 * a CMS.
			 */
			public static final String X_FORWARDED_FOR = "X-Forwarded-For";		

			/**
			 * Header used to provide the server hostname which was requested. Multiple
			 * hostnames may be served by a single Funnelback server over a single IP 
			 * address.
			 */
			public static final String HOST = "host";		

			/**
			 * Header used to provide the URL of the web page from which the user accessed
			 * the search service.
			 */
			public static final String REFERRER = "Referrer";		
		}

		/**
		 * Common request parameter names used in the cache
		 * controller.
		 * 
		 * @since 11.0
		 */
		public static class Cache {
			
			/**
			 * Used in automated tests, in click tracking, to return directly the content
			 * of a file instead of redirecting to it.
			 */
			public static final String NOATTACHMENT = "noattachment";
			
			/**
			 * Used in click tracking. Target URL to redirect to.
			 */
			public static final String INDEX_URL = "index_url";
		}
		
		/**
		 * Common request parameter names used in the click
		 * controller.
		 * 
		 * @since 11.0
		 */
		public static class Click {
			public static final String URL = "url";
			public static final String INDEX_URL = "index_url";
			public static final String AUTH = "auth";
			public static final String SEARCH_REFERER = "search_referer";
			public static final String TYPE = "type";
			
			public static final String TYPE_FP = "FP";
		}

		/**
		 * Common request parameter names used in the <tt>serve-*</tt>
		 * controllers
		 * 
		 * @since 11.0
		 */
		public static class Serve {
			public static final String URI = "uri";
			public static final String DOC = "doc";
		}
		
		/**
		 * Common request parameter names used in
		 * contextual navigation.
		 * 
		 * @since 11.0
		 *
		 */
		public static class ContextualNavigation {
			/** Name of the last clicked suggestion */
			public static final String CN_CLICKED = "clicked_fluster";
			
			/**
			 * Prefix for the previously clicked
			 * suggestions (cluster0, cluster1, etc).
			 */
			public static final String CN_PREV_PREFIX = "cluster";
			
			/**
			 * Pattern to use to find previously clicked
			 * suggestion parameters.
			 */
			public static final Pattern CN_PREV_PATTERN = Pattern.compile(CN_PREV_PREFIX + "\\d+");
		}
	}
	
}
