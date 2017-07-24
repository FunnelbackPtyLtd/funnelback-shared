package com.funnelback.publicui.search.service.config;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Repository;

import com.funnelback.common.config.Collection.Type;
import com.funnelback.common.config.CollectionId;
import com.funnelback.common.config.Config;
import com.funnelback.common.config.ConfigReader;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.common.config.GlobalOnlyConfig;
import com.funnelback.common.groovy.GroovyLoader;
import com.funnelback.common.views.View;
import com.funnelback.config.configtypes.server.DefaultServerConfigReadOnly;
import com.funnelback.config.configtypes.server.ServerConfigReadOnly;
import com.funnelback.config.data.server.ServerConfigData;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Collection.Hook;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.collection.paramtransform.TransformRule;
import com.funnelback.publicui.search.model.curator.config.Configurer;
import com.funnelback.publicui.search.model.curator.config.CuratorConfig;
import com.funnelback.publicui.search.model.curator.config.CuratorYamlConfig;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.resource.impl.ConfigMapResource;
import com.funnelback.publicui.search.service.resource.impl.CuratorJsonConfigResource;
import com.funnelback.publicui.search.service.resource.impl.CuratorYamlConfigResource;
import com.funnelback.publicui.search.service.resource.impl.FacetedNavigationConfigResource;
import com.funnelback.publicui.search.service.resource.impl.ParameterTransformResource;
import com.funnelback.publicui.search.service.resource.impl.SimpleFileResource;
import com.funnelback.publicui.search.service.resource.impl.UniqueLinesResource;
import com.funnelback.publicui.utils.MapUtils;
import com.funnelback.publicui.xml.FacetedNavigationConfigParser;
import com.funnelback.springmvc.service.resource.ResourceManager;
import com.funnelback.springmvc.service.resource.impl.AbstractSingleFileResource;
import com.funnelback.springmvc.service.resource.impl.GroovyCollectionLoaderResource;
import com.funnelback.springmvc.service.resource.impl.PropertiesResource;
import com.funnelback.springmvc.service.resource.impl.config.CollectionConfigResource;
import com.funnelback.springmvc.service.resource.impl.config.GlobalConfigResource;
import com.funnelback.springmvc.service.resource.impl.config.ServerConfigDataResource;

import groovy.lang.Script;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * <p>Default {@link ConfigRepository} implementation.</p>
 * 
 * <p>Relies on a {@link ResourceManager} to cache access to
 * the underlying files</p>
 */
@Repository("configRepository")
@Log4j2
public class DefaultConfigRepository implements ConfigRepository {
            
    public static final String FTL_SUFFIX = ".ftl";
    protected static final String CFG_SUFFIX = ".cfg";
    
    private static final Pattern FORM_BACKUP_PATTERN = Pattern.compile("-\\d{12}"+FTL_SUFFIX);
    
    private static final String EXTRA_SEARCHES_PREFIX = "extra_search";
    
    private static final String CACHE = "localConfigRepository";
    
    /**
     * <p>Cache TTL. No need to set it to less than 1s because
     * ext3 time resolution is 1s anyway.</p>
     */
    @Value("#{appProperties['config.repository.cache.ttl']?:1}")
    @Setter
    private int cacheTtlSeconds = 1;
    
    /**
     * <p>How often to force reloading of Groovy script, in seconds.</p>
     * 
     * <p>This is set to 1000 years (not too big to avoid surprise overflows), 
     * as we shouldn't need to reload the the groovy class loader. This option is
     * left in place in case the GroovyEngineScript doesn't work as expected
     * and we do need to revert the behaviour.</p>
     * 
     * @see FUN-8961
     */
    @Value("#{appProperties['config.repository.groovy.reload']?:31536000000}")
    @Setter
    private long groovyForceReloadSeconds = TimeUnit.SECONDS.convert(365*1000, TimeUnit.DAYS);
    
    @Autowired
    @Setter
    protected CacheManager appCacheManager;
    
    @Autowired
    @Setter
    protected ResourceManager resourceManager;

    @Autowired
    @Setter
    protected File searchHome;
    
    @Autowired
    @Setter
    private FacetedNavigationConfigParser fnConfigParser;
    
    @Autowired
    @Setter
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    /**
     * <p>This implementation will cache collection objects for a short period
     * so that multiple call to this function for a single request will
     * not cause the Collection object to be "rebuilt" from all configuration
     * data.</p>
     * 
     * <p>Note that the configuration data is cached anyway by the {@link ResourceManager}
     * abstraction, however getting all the configuration for the cache and assembling it
     * is still a bit expensive.</p>
     */
    @Override
    public Collection getCollection(String collectionId) {
        // Cache will never be null
        Cache cache = appCacheManager.getCache(CACHE);
        Element elt = cache.get(collectionId);
        
        if (elt == null) {
            try {
                Collection c = loadCollection(collectionId);
                Element e = new Element(collectionId, c);
                // Expire element after 1 second so that it's short-lived
                // only for the current request.
                e.setTimeToLive(cacheTtlSeconds);
                cache.put(e);
                return c;
            } catch (IOException ioe) {
                log.error("Unable to load collection '" + collectionId + "'", ioe);
                return null;
            }
        } else {
            return (Collection) elt.getObjectValue();
        }
    }
    
    /**
     * Loads a collection
     * @param collectionId Collection to load
     * @return The collection object, or null if not found
     * @throws IOException
     */
    private Collection loadCollection(String collectionId) throws IOException {
        log.debug("Loading collection configuration for '"+collectionId+"'");
        
        // Sanity check the layout - The parent of the collection's directory should be $SEARCH_HOME/conf
        File configFolder = new File(searchHome+File.separator+DefaultValues.FOLDER_CONF).getCanonicalFile();
        File collectionConfigFolder = new File(configFolder, collectionId).getCanonicalFile();
        if (!(collectionConfigFolder.getParentFile().equals(configFolder))) {
            log.debug("Collection directory parent '"+collectionConfigFolder.getParentFile()+
                "' is not Funnelback config directory '"+configFolder+
                "' for collectionId '"+collectionId+"'");
            return null;
        }
        
        Config config = resourceManager.load(new CollectionConfigResource(searchHome, new CollectionId(collectionId)));
        if (config == null) {
            return null;
        }
        
        Collection c = new Collection(collectionId, config);
        
        loadFacetedNavigationConfig(c);
        
        if (Type.meta.equals(c.getType())) {
            c.setMetaComponents(
                    resourceManager.load(
                            new SimpleFileResource(new File(collectionConfigFolder, Files.META_CONFIG_FILENAME)),
                            AbstractSingleFileResource.wrapDefault(new String[0])).getResource());
        } else {
            c.setMetaComponents(new String[0]);
        }

        c.getTextMinerBlacklist().addAll(
                resourceManager.load(
                        new UniqueLinesResource(new File(collectionConfigFolder, Files.TEXT_MINER_BLACKLIST)),
                        AbstractSingleFileResource.wrapDefault(new HashSet<String>(0))).getResource());

        c.getParametersTransforms().addAll(
                resourceManager.load(
                        new ParameterTransformResource(new File(collectionConfigFolder, Files.CGI_TRANSFORM_CONFIG_FILENAME)),
                        AbstractSingleFileResource.wrapDefault(new ArrayList<TransformRule>(0))).getResource());
        c.setQuickLinksConfiguration(resourceManager.load(new ConfigMapResource(
                collectionId,
                searchHome,
                new File(collectionConfigFolder, Files.QUICKLINKS_CONFIG_FILENAME)), 
                AbstractSingleFileResource.wrapDefault(new HashMap<String, String>(0))).getResource());
        
        c.getProfiles().putAll(loadProfiles(c));
        
        // Force the use of the same groovy loader for all of our scripts. It is probably less suprising for users
        // that way.
        GroovyLoader collectionGroovyLoader = resourceManager.load(
            new GroovyCollectionLoaderResource(searchHome, new CollectionId(collectionId), groovyForceReloadSeconds));
        
        for (Hook hook: Hook.values()) {
            File hookScriptFile = new File(collectionConfigFolder, Files.HOOK_PREFIX + hook.toString() + Files.HOOK_SUFFIX);
            loadScriptHandleExceptions(collectionGroovyLoader, hookScriptFile)
                .ifPresent(hookScript -> c.getHookScriptsClasses().put(hook, hookScript));
        }
        
        loadScriptHandleExceptions(collectionGroovyLoader, new File(collectionConfigFolder, Files.CART_PROCESS_PREFIX + Files.GROOVY_SUFFIX))
            .ifPresent(c::setCartProcessClass);
        
        return c;
    }
    
    private Optional<Class<Script>> loadScriptHandleExceptions(GroovyLoader groovyLoader, File hookScriptFile) {
        if (hookScriptFile.exists()) {
            try {
                return Optional.of(groovyLoader.loadScript(hookScriptFile));
                
            } catch (CompilationFailedException cfe) {
                log.error("Compilation of hook script '"+hookScriptFile+"' failed", cfe);
            } catch (IOException | ResourceException | ScriptException e) {
                log.error("Error in hook script '"+hookScriptFile+"' failed", e);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Loads the profiles for a given collection
     * @param c
     * @return
     */
    private Map<String, Profile> loadProfiles(Collection c) {
        HashMap<String, Profile> out = new HashMap<String, Profile>();
        
        for (File profileDir: getProfileDirs(c.getConfiguration().getConfigDirectory())) {
            Profile p = new Profile(profileDir.getName());
            loadFacetedNavigationConfig(c, p);
            File padreOptsFile = new File(profileDir + File.separator + Files.PADRE_OPTS);
            try {
                p.setPadreOpts(resourceManager.load(new AbstractSingleFileResource<String>(padreOptsFile) {
                        @Override
                        public String parseResourceOnly() throws IOException {
                            return FileUtils.readFileToString(file);
                        }
                    }, AbstractSingleFileResource.wrapDefault(null)).getResource());
            } catch (IOException e) {
                log.error("Could not read padre opts file from '"+padreOptsFile+"'",e);
            }

            
            CuratorConfig config = new CuratorConfig();  // Empty default curator config
            
            // Load curator config from each of the supported config files (combining them)
            try {
                File curatorJsonConfigFile = new File(profileDir, Files.CURATOR_JSON_CONFIG_FILENAME);
                config.addAll(resourceManager.load(new CuratorJsonConfigResource(curatorJsonConfigFile), 
                    AbstractSingleFileResource.wrapDefault(new CuratorConfig())).getResource().getTriggerActions());
            } catch (IOException e) {
                log.error("Error loading curator json configuration.", e);
            }
            
            try {
                File curatorAdvancedJsonConfigFile = new File(profileDir, Files.CURATOR_JSON_ADVANCED_CONFIG_FILENAME);
                config.addAll(resourceManager.load(new CuratorJsonConfigResource(curatorAdvancedJsonConfigFile), 
                    AbstractSingleFileResource.wrapDefault(new CuratorConfig())).getResource().getTriggerActions());
            } catch (IOException e) {
                log.error("Error loading curator advanced json configuration.", e);
            }

            try {
                File curatorYamlConfigFile = new File(profileDir, Files.CURATOR_YAML_CONFIG_FILENAME);
                config.addAll(resourceManager.load(new CuratorYamlConfigResource(curatorYamlConfigFile), 
                    AbstractSingleFileResource.wrapDefault(new CuratorYamlConfig())).getResource().toTriggerActions());
            } catch (IOException e) {
                log.error("Error loading curator yaml configuration.", e);
            }
            
            // Autowire in anything the Curator objects depend on
            config.configure(new Configurer() {
                @Override
                public void configure(Object objectToConfigure) {
                    if (autowireCapableBeanFactory != null) {
                        autowireCapableBeanFactory.autowireBean(objectToConfigure);
                    } else {
                        log.error("Expected AutowireBeanFactory bean to be available - Some curator rules may not function.");
                    }
                }
            });
            
            p.setCuratorConfig(config);

            out.put(p.getId(), p);
            log.debug("Loaded profile from '" + profileDir.getAbsolutePath() + "' for collection '" + c.getId() + "'");
        }
        
        return out;        
    }

    /**
     * Returns the list of profiles for a given collection, by looking
     * in the collection configuration folder
     * @param configDir
     * @return
     */
    private File[] getProfileDirs(File configDir) {
        return configDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                // Only directories that doesn't starts with a dot (.svn ...)
                return pathname.isDirectory() && !pathname.getName().startsWith(".");
            }
        });
    }

    /**
     * Loads faceted_navigation.cfg, for a collection, from multiple locations:
     * - Global in conf/faceted_navigation.cfg
     * - Live in live/idx/faceted_navigation.xml
     * @param c
     * @return
     */
    private void loadFacetedNavigationConfig(Collection c) {
        // Read global config in conf/
        File fnConfig = new File (c.getConfiguration().getConfigDirectory(), Files.FACETED_NAVIGATION_CONFIG_FILENAME);
        try {
            c.setFacetedNavigationConfConfig(resourceManager.load(new FacetedNavigationConfigResource(fnConfig, fnConfigParser), AbstractSingleFileResource.wrapDefault(null)).getResource());
            
            // Read config in live/idx/
            fnConfig = new File(c.getConfiguration().getCollectionRoot()
                    + File.separator + View.live
                    + File.separator + DefaultValues.FOLDER_IDX,
                    Files.FACETED_NAVIGATION_LIVE_CONFIG_FILENAME);
            c.setFacetedNavigationLiveConfig(resourceManager.load(new FacetedNavigationConfigResource(fnConfig, fnConfigParser), AbstractSingleFileResource.wrapDefault(null)).getResource());
        } catch (IOException ioe) {
            log.error("Unable to read faceted navigation configuration from '" + fnConfig.getAbsolutePath() + "'", ioe);
        }
    }
    
    /**
     * Loads faceted_navigation.cfg, for a profile, from multiple locations:
     * - Global in conf/<profile>/faceted_navigation.cfg
     * - Live in live/idx/<profile>/faceted_navigation.xml
     * @param c
     * @param p
     */
    private void loadFacetedNavigationConfig(Collection c, Profile p) {
        // Read global config in conf/<profile>/
        File fnConfig = new File (c.getConfiguration().getConfigDirectory() + File.separator + p.getId(), Files.FACETED_NAVIGATION_CONFIG_FILENAME);
        try {
            p.setFacetedNavConfConfig(resourceManager.load(new FacetedNavigationConfigResource(fnConfig, fnConfigParser), AbstractSingleFileResource.wrapDefault(null)).getResource());
            
            // Read config in live/idx/<profile>/
            fnConfig = new File(c.getConfiguration().getCollectionRoot()
                    + File.separator + View.live
                    + File.separator + DefaultValues.FOLDER_IDX
                    + File.separator + p.getId(),
                    Files.FACETED_NAVIGATION_LIVE_CONFIG_FILENAME);
        } catch (IOException ioe) {
            log.error("Unable to read faceted navigation configuration from '" + fnConfig.getAbsolutePath() + "'", ioe);
        }
    }

    @Override
    public List<String> getAllCollectionIds() {
        File configDirectory = new File(searchHome, DefaultValues.FOLDER_CONF);
        File[] collectionDirs = configDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                // Only directories that doesn't starts with a dot (.svn ...)
                return pathname.isDirectory() && !pathname.getName().startsWith(".");
            }
        });
            
        List<String> collectionIds = new ArrayList<String>();
        for (File collectionDir : collectionDirs) {
            collectionIds.add(collectionDir.getName());
        }
        Collections.sort(collectionIds);
        
        return collectionIds;            
    }
    
    @Override
    public Map<String, String> getGlobalConfigurationFile(GlobalConfiguration conf) {
        try {
            return resourceManager.load(
                    new ConfigMapResource(searchHome,
                            new File(searchHome+File.separator+DefaultValues.FOLDER_CONF, conf.getFileName())), AbstractSingleFileResource.wrapDefault(null)).getResource();
        } catch (IOException ioe) {
            log.fatal("Could not load global configuration file '"+conf.getFileName()+"'", ioe);
            throw new RuntimeException(ioe);
        }
    }    
    
    @Override
    public List<Collection> getAllCollections() {
        List<Collection> collections = new ArrayList<Collection>();
        for (String collectionId: getAllCollectionIds()) {
            try {
                Collection collection = getCollection(collectionId);
                if (collection != null) {
                    collections.add(collection);
                }
            } catch (Exception e) {
                log.warn("Error while loading collection '"+collectionId+"'", e);
            }
        }
        return collections;
    }

    /**
     * Loads the executables config file
     */
    
    public String getExecutablePath(String exeName) {
        File executablesCfg = new File(new File(searchHome, DefaultValues.FOLDER_CONF), Files.EXECUTABLES_CONFIG_FILENAME);
        try {
            Map<String, String> m = resourceManager.load(
                new AbstractSingleFileResource<Map<String, String>>(executablesCfg) {

                    @Override
                    public Map<String, String> parseResourceOnly() throws IOException {
                        Map<String, String> out = new HashMap<String, String>();
                        Map<String, String> uncleanMap = ConfigReader.readConfig(file, searchHome);
                        
                        // replace quotes at the ends of the executable names (if any)
                        for(Entry<String,String> entry : uncleanMap.entrySet()) {
                            String value = entry.getValue();
                            if(value != null) {
                                if((value.charAt(0) == '"' && value.charAt(value.length() -1) == '"' )|| (value.charAt(0) == '\'' && value.charAt(value.length() -1) == '\'' )) {
                                    value = value.substring(1, value.length() -1);    
                                }
                            }
                            out.put(entry.getKey(), value);
                        }
                        
                        return out;
                    }

                    
                }, AbstractSingleFileResource.wrapDefault(null)).getResource();
            return m.get(exeName);
        } catch (Exception ioe) {
            log.error("Could not load executables config from '"+executablesCfg+"'", ioe);
            throw new RuntimeException(ioe);
        }
    }

    public String[] getForms(String collectionId, String profileId) {
        if (collectionId == null || profileId == null) {
            throw new IllegalArgumentException("collectionId and profileId cannot be null");
        }
        Collection c = getCollection(collectionId);
        if (c == null) {
            throw new IllegalArgumentException("Invalid collection '" + collectionId + "'");
        }
        
        // Find form files, excluding backups (simple-20110101093000.ftl)
        File profileDir = new File(c.getConfiguration().getConfigDirectory() + File.separator + profileId);
        if (!profileDir.exists()) {
            return new String[0];
        }
        
        File[] formFiles = profileDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile()
                    && pathname.getName().endsWith(FTL_SUFFIX)
                    && ! FORM_BACKUP_PATTERN.matcher(pathname.getName()).matches();
            }
        });
        
        // Remove .ftl suffix
        String[] out = new String[formFiles.length];
        for (int i=0; i<formFiles.length; i++) {
            out[i] = formFiles[i].getName().replaceAll(FTL_SUFFIX+"$", "");
        }
        
        return out;
        
    }
    
    public Map<String, String> getExtraSearchConfiguration(Collection collection, String extraSearchId) {
        File config = new File(collection.getConfiguration().getConfigDirectory(),
                EXTRA_SEARCHES_PREFIX + "." + extraSearchId + CFG_SUFFIX);
        
        try {
            Properties p = resourceManager.load(new PropertiesResource(config), AbstractSingleFileResource.wrapDefault(null)).getResource();
            if (p != null) {
                return MapUtils.fromProperties(p);
            } else {
                log.warn("Extra searches configuration file '"+config+"' doesn't exist");
            }
        } catch (IOException ioe) {
            log.error("Could not load extra seach config file from '"+config+"'", ioe);
        }
        
        return null;
    }

    @Override
    public GlobalOnlyConfig getGlobalConfiguration() {
        try {
            return resourceManager.load(new GlobalConfigResource(searchHome));
        } catch (IOException ioe) {
            log.fatal("Could not load global configuration", ioe);
            throw new RuntimeException(ioe);
        }
    }
    
    @Override
    public Map<String, String> getTranslations(String collectionId,    String profileId, Locale locale) {
        Map<String, String> out = new HashMap<String, String>();
        Map<String, String> emptyMap = new HashMap<String, String>(0);
        
        // Possible files: Global config, language specific,
        // language + country specific
        List<String> filenames = new ArrayList<String>();
        filenames.add(Files.UI_I18N);
        if (locale.getLanguage() != null) {
            filenames.add(Files.UI_I18N_PREFIX + "." + locale.getLanguage() + Files.UI_I18N_SUFFIX);
            if (locale.getCountry() != null) {
                filenames.add(Files.UI_I18N_PREFIX + "." + locale.getLanguage() + "_" + locale.getCountry() + Files.UI_I18N_SUFFIX);
            }
        }

        // Folders to look into
        List<File> folders = new ArrayList<File>();
        folders.add(new File(searchHome, DefaultValues.FOLDER_CONF));
        if (profileId != null && ! "".equals(profileId)) {
                folders.add(new File(searchHome
                        + File.separator + DefaultValues.FOLDER_CONF
                        + File.separator + collectionId,
                        profileId));
        };

        // Lookup general files first, then more specific ones
        for (String filename: filenames) {
            for (File folder: folders) {
                File f = new File(folder, filename);
                try {
                    out.putAll(resourceManager.load(new ConfigMapResource(searchHome, f), AbstractSingleFileResource.wrapDefault(emptyMap)).getResource());
                } catch (IOException ioe) {
                    log.error("Unable to load translation bundle from '"+f.getAbsolutePath()+"'", ioe);
                }

            }
        }
        
        return out;
    }
    
    @Override
    public File getXslTemplate(String collectionId, String profileId) {
        File template = new File(searchHome
                + File.separator + DefaultValues.FOLDER_CONF
                + File.separator + collectionId
                + File.separator + profileId,
                Files.XSL_TEMPLATE);
        
        if (template.exists()) {
            return template;
        } else {
            return null;
        }
    }

    @Override
    public ServerConfigReadOnly getServerConfig() {
        ServerConfigData serverConfigData = resourceManager.loadResource(new ServerConfigDataResource(searchHome, (f, r) -> {
            throw new RuntimeException("Writes are not permitted under the public UI.");
        }));
        return new DefaultServerConfigReadOnly(serverConfigData);
    }
    
}
