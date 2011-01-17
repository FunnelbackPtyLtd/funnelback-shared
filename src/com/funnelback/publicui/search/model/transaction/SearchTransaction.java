package com.funnelback.publicui.search.model.transaction;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Represents a full search transaction consisting of 
 * a question, a response and possible error.
 */
@RequiredArgsConstructor
public class SearchTransaction {

	@Getter private final SearchQuestion question;
	@Getter private final SearchResponse response;
	@Getter @Setter private SearchError error;
	
	public boolean hasResponse() { return response != null; }
	
}
