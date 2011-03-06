package com.funnelback.publicui.search.lifecycle.input.processors.explore;

import com.funnelback.publicui.search.model.collection.Collection;

/**
 * Generate an explore query for a specific URL
 */
public interface ExploreQueryGenerator {

	/**
	 * Generate an explore query
	 * @param c Collection to generate the query from
	 * @param url Document URL
	 * @param nbOfTerms number of query terms. Can be null to use the PADRE default.
	 * @return Query terms, or null
	 */
	public String getExploreQuery(Collection c, String url, Integer nbOfTerms);
	
}
