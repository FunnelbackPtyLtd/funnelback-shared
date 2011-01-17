package com.funnelback.publicui.search.service;

import java.util.List;

import com.funnelback.publicui.search.model.Collection;

/**
 * Interface to access collection / search service configuration
 */
public interface ConfigRepository {

	/**
	 * @param collectionId ID of the collection (technical name)
	 * @return A {@link Collection}
	 */
	public Collection getCollection(String collectionId);
	
	/**
	 * @return All available collections on this repository
	 */
	public List<Collection> getAllCollections();
	
	/**
	 * Is supposed to be less resource-intensive than #getAllCollections
	 * @return All available collections IDs on this repository.
	 */
	public List<String> getAllCollectionIds();
	
}
