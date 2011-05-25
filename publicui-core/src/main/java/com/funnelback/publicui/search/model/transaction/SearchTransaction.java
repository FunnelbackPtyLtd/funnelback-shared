package com.funnelback.publicui.search.model.transaction;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.FutureTask;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.apachecommons.Log;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Represents a full search transaction consisting of 
 * a question, a response and possible error.
 */
@RequiredArgsConstructor
@JsonIgnoreProperties({"extraSearchesTasks", "extraSearchesQuestions"})
@Log
public class SearchTransaction {

	/**
	 * Key to use for the extra search related to faceted navigation.
	 */
	public static final String EXTRA_SEARCH_FACETED_NAV = "__faceted_nav__";
	
	@Getter private final SearchQuestion question;
	@Getter private final SearchResponse response;
	@Getter @Setter private SearchError error;
	
	/**
	 * Any additional extra search transactions performed during this transaction.
	 */
	@Getter private final Map<String, SearchTransaction> extraSearches = new HashMap<String, SearchTransaction>();

	/**
	 * Additional {@link SearchQuestion}s to process as extra searches.
	 */
	@XStreamOmitField
	@Getter private final Map<String, SearchQuestion> extraSearchesQuestions = new HashMap<String, SearchQuestion>();
	
	/**
	 * Internal holder of extra searches tasks
	 */
	@XStreamOmitField
	@Getter private final Map<String, FutureTask<SearchTransaction>> extraSearchesTasks = new HashMap<String, FutureTask<SearchTransaction>>();
	
	/**
	 * Adds an {@link SearchQuestion} to be processed as an extra search.
	 * @param key
	 * @param q
	 */
	public void addExtraSearch(String key, SearchQuestion q) {
		extraSearchesQuestions.put(key, q);
	}	
	
	public boolean hasResponse() { return response != null; }
	public boolean hasQuestion() { return question != null; }
	
}
