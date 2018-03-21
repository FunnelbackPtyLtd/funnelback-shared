package com.funnelback.publicui.search.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import java.io.File;

import com.funnelback.common.config.CollectionNotFoundException;
import com.funnelback.common.config.Files;
import com.funnelback.common.config.GlobalOnlyConfig;
import com.funnelback.common.profile.ProfileNotFoundException;
import com.funnelback.config.configtypes.index.IndexConfigReadOnly;
import com.funnelback.config.configtypes.server.ServerConfigReadOnly;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.publicui.search.model.collection.Collection;

import lombok.Getter;


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
     * @param collectionId ID of the collection (technical name)
     * @param profileIdAndView ID of the profile (with optional '_preview' suffix for preview view)
     * @return A {@link ServiceConfigReadOnly} for the given service.
     * @throws ProfileNotFoundException if the profile requested does not exist
     */
    public ServiceConfigReadOnly getServiceConfig(String collectionId, String profileIdAndView) throws ProfileNotFoundException;

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
    public GlobalOnlyConfig getGlobalConfiguration();
    
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
    
    public ServerConfigReadOnly getServerConfig();
    
    /**
     * Get a read only IndexConfig
     *  
     * @param collectionId
     * @return
     * @throws CollectionNotFoundException
     */
    public IndexConfigReadOnly getIndexConfig(String collectionId) throws CollectionNotFoundException;

}
