package com.funnelback.publicui.search.service;

import java.util.List;
import java.util.Map;

import lombok.Getter;

import com.funnelback.common.config.Files;
import com.funnelback.publicui.search.model.collection.Collection;


/**
 * Interface to access collection / search service configuration
 */
public interface ConfigRepository {

	public static enum GlobalConfiguration {
		DNSAliases(Files.DNS_ALIASES_FILENAME);
		
		@Getter private final String fileName;
		
		private GlobalConfiguration(String fileName) {
			this.fileName = fileName;
		}
	}
	
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
	
	/**
	 * Loads a global configuration file. 
	 * @param conf File to read
	 * @return
	 */
	public Map<String, String> getGlobalConfiguration(GlobalConfiguration conf);

}
