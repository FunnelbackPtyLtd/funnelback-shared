package com.funnelback.publicui.search.service;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Getter;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Files;
import com.funnelback.publicui.search.model.collection.Collection;


/**
 * Interface to access collection / search service configuration
 */
public interface ConfigRepository {

    public static enum GlobalConfiguration {
        DNSAliases(Files.DNS_ALIASES_FILENAME),
        GlobalCfg(null);    // global.cfg.default and global.cfg. Special case
        
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
    public Map<String, String> getGlobalConfigurationFile(GlobalConfiguration conf);
    
    /**
     * @return the global.cfg(.default) configuration data
     */
    public Config getGlobalConfiguration();
    
    /**
     * @param exeName The key used to store the executable location 
     * @return the path to the given executable, as stored in executables.cfg.
     */
    public String getExecutablePath(String exeName);
    
    /**
     * @param collectionId
     * @param profileId
     * @return The list of available forms for a collection, for a given profile
     */
    public String[] getForms(String collectionId, String profileId);
    
    /**
     * @param collectionId
     * @param profileId
     * @return The XSL template for a collection, for a given profile, or null if not set 
     */
    public File getXslTemplate(String collectionId, String profileId);
    
    /**
     * @param collection
     * @param extraSearchId
     * @return The extra search configuration data for the given collection and extra search source.
     */
    public Map<String, String> getExtraSearchConfiguration(Collection collection, String extraSearchId);
    
    /**
     * Gets the translations for a given collection, profile and locale
     * @param collectionId
     * @param profileId
     * @param locale
     * @return
     */
    public Map<String, String> getTranslations(String collectionId, String profileId, Locale locale);

}
