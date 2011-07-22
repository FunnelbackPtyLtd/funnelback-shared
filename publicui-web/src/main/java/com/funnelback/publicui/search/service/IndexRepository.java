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
		indexer_arguments,
		/** Numbers of documents in the collection */
		Num_docs
	}
	
	/** Separator for the indexer options of the <code>.bldinfo</code> file */
	public static final String INDEXER_OPTIONS_SEPARATOR = "\n";
	
	/**
	 * @param c
	 * @return The last update date for the collection
	 * @throws UnsupportedOperationException if the collection is a meta collection,
	 * since they don't have a <code>index_time</code> file.
	 */
	public Date getLastUpdated(String collectionId) throws UnsupportedOperationException;
	
	/**
	 * Retrieve a value from the <code>.bldinfo<code> file.
	 * @param key
	 * @return
	 * @throws UnsupportedOperationException if the collection is a meta collection,
	 * since they don't have a <code>.bldinfo</code> file.
	 */
	public String getBuildInfoValue(String collectionId, String key) throws UnsupportedOperationException;
}
