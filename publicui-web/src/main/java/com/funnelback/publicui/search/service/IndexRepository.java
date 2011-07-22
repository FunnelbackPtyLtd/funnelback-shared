package com.funnelback.publicui.search.service;

import java.util.Date;

/**
 * Interface to access index data
 */
public interface IndexRepository {
	
	/**
	 * Keys used to retrieve some special values
	 * from the <code>.bldinfo</code> map.
	 */
	public enum BuildInfoKeys {
		/** PADRE version string (Starting with 'version') */
		version,
		/** List of indexer arguments, separated by {@link #INDEXER_OPTIONS_SEPARATOR} */
		indexer_arguments
	}
	
	/** Separator for the indexer options of the <code>.bldinfo</code> file */
	public static final String INDEXER_OPTIONS_SEPARATOR = "\n";
	
	/**
	 * @param c
	 * @return The last update date for the collection
	 */
	public Date getLastUpdated(String collectionId);
	
	/**
	 * Retrieve a value from the <code>.bldinfo<code> file.
	 * @param key
	 * @return
	 */
	public String getBuildInfoValue(String collectionId, String key);
}
