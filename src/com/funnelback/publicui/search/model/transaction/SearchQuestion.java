package com.funnelback.publicui.search.model.transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.model.Collection;

@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SearchQuestion {
		
	@Getter @Setter private String query;
	@Getter @Setter private Collection collection;
	@Getter @Setter private String profile = DefaultValues.DEFAULT_PROFILE;
	@Getter @Setter private String[] clive;
	
	/**
	 * meta_* / query_* parameters, transformed as query expressions.
	 */
	@Getter final private Map<String, String> metaParameters = new HashMap<String, String>();
	
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
	 * Whether the request is impersonated
	 */
	@Getter @Setter private boolean impersonated;
	
	public static class RequestParameters {
		public static final String COLLECTION = "collection";
		public static final String QUERY = "query";
		public static final String CLIVE = "clive";
		
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
		
		public static class Serve {
			public static final String URI = "uri";
			public static final String DOC = "doc";
		}
	}
	
}
