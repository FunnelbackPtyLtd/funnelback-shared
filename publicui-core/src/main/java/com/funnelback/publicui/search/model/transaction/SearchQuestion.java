package com.funnelback.publicui.search.model.transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.log.Log;

@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SearchQuestion {
		
	@Getter @Setter private String query;
	@Getter @Setter private String originalQuery;
	@Getter @Setter private Collection collection;
	
	/**
	 * Search profile, defaulting to "_default"
	 */
	@Getter @Setter private String profile = DefaultValues.DEFAULT_PROFILE;
	
	/**
	 * Specific component of a meta-collection to query
	 */
	@Getter @Setter private String[] clive;
	
	/**
	 * Display form (template)
	 */
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
	 * Query expressions, will be passed to PADRE
	 */
	@Getter final private List<String> queryExpressions = new ArrayList<String>();
	
	/**
	 * meta_* / query_* parameters, transformed as query expressions.
	 */
	@Getter final private List<String> metaParameters = new ArrayList<String>();
	
	/**
	 * Additional parameters to pass as-is to PADRE
	 */
	@Getter final private Map<String, String[]> additionalParameters = new HashMap<String, String[]>();
	
	/**
	 * List of environment variables to repass to PADRE
	 */
	@Getter final private Map<String, String> environmentVariables = new HashMap<String, String>();

	/**
	 * Dynamic QP options for PADRE, in addition to the one set in
	 * collection.cfg.
	 * 
	 * Will be updated by the {@link InputProcessor}s
	 */
	@Getter final private List<String> dynamicQueryProcessorOptions = new ArrayList<String>();

	/**
	 * User keys for early binding DLS
	 */
	@Getter final private List<String> userKeys = new ArrayList<String>();
	
	/**
	 * Selected facets categories
	 */
	@Getter final private Map<String, List<String>> selectedCategories = new HashMap<String, List<String>>();
	
	/**
	 * Whether the request is impersonated
	 */
	@Getter @Setter private boolean impersonated;
	
	/**
	 * User identifier for this transaction. Depending of the collection configuration
	 * it can be an IP address, an md5 hash of the address, nothing ("-") or null. 
	 */
	@Getter @Setter private String userId = Log.USERID_NOTHING;
	
	/**
	 * Input parameters map. In a Web servlet context will contain the request parameters map.
	 */
	@Getter private final Map<String, String[]> inputParameterMap = new HashMap<String, String[]>();
	
	public static class RequestParameters {
		public static final String COLLECTION = "collection";
		public static final String QUERY = "query";
		public static final String PROFILE = "profile";
		public static final String CLIVE = "clive";
		public static final String GSCOPE1 = "gscope1";
		
		/** Explore: Number of query terms */
		public static final String EXP = "exp";
		
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
		
		public static class Click {
			public static final String URL = "url";
			public static final String INDEX_URL = "index_url";
			public static final String AUTH = "auth";
			public static final String SEARCH_REFERER = "search_referer";
			public static final String TYPE = "type";
			
			public static final String TYPE_FP = "FP";
		}
		
		public static class Serve {
			public static final String URI = "uri";
			public static final String DOC = "doc";
		}
		
		public static final String FACET_PREFIX = "f.";
		
		public static class ContextualNavigation {
			public static final String CN_CLICKED = "clicked_fluster";
			public static final String CN_PREV_PREFIX = "cluster";
			
			public static final Pattern CN_PREV_PATTERN = Pattern.compile(CN_PREV_PREFIX + "\\d+");
		}
	}
	
}
