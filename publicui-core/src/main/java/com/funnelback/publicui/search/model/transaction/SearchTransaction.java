package com.funnelback.publicui.search.model.transaction;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.FutureTask;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Represents a full search transaction consisting of 
 * a question, a response and possible error.
 */
@RequiredArgsConstructor
@JsonIgnoreProperties({"extraSearchesTasks"})
public class SearchTransaction {

	@Getter private final SearchQuestion question;
	@Getter private final SearchResponse response;
	@Getter @Setter private SearchError error;
	
	/**
	 * Any additional extra search performed during this transaction.
	 */
	@Getter private final Map<String, SearchTransaction> extraSearches = new HashMap<String, SearchTransaction>();

	@XStreamOmitField
	private final Map<String, FutureTask<SearchTransaction>> extraSearchesTasks = new HashMap<String, FutureTask<SearchTransaction>>();
	
	/**
	 * Adds an extra search task. Assumes it has already been started
	 * @param key
	 * @param task
	 */
	public void addExtraSearch(String key, FutureTask<SearchTransaction> task) {
		extraSearchesTasks.put(key, task);
	}
	
	/**
	 * Wait for all pending extra searches task to complete,
	 * and fill {@link #extraSearches}.
	 */
	public void joinExtraSearches() {
		for (String key: extraSearchesTasks.keySet()) {
			try {
				extraSearches.put(key, extraSearchesTasks.get(key).get());
			} catch (Exception e) {
			}
		}
	}
	
	
	public boolean hasResponse() { return response != null; }
	public boolean hasQuestion() { return question != null; }
	
}
