package com.funnelback.publicui.search.service;

import java.util.Date;

/**
 * Interface to access index data
 */
public interface IndexRepository {
	
	/**
	 * @param c
	 * @return The last update date for the collection
	 */
	public Date getLastUpdated(String collectionId);
}
