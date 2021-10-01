package com.funnelback.filter.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * An immutable context supplied to the filters.
 * 
 * <p>The context can be used to get values from the collection configuration the filter 
 * is running under.</p>
 *
 */
public interface FilterContext extends InternalFilterContext {

    /**
     * Gets the name of the collection under which the filter is run.
     * 
     * @return the collection name the filter is running under.
     */
    public String getCollectionName();
    
    /**
     * Gets the set of all configuration keys for collection the filter is running under.
     * 
     * @return all configurations keys.
     */
    public Set<String> getConfigKeys();
    
    /**
     * Gets the value of the configuration setting for the collection the filter is running under.
     * 
     * @param key used to lookup the configuration value.
     * @return the value for the key from the configuration if the key exists in the configuration
     * otherwise returns empty.
     */
    public Optional<String> getConfigValue(String key);
    
    /**
     * Provides a list of config setting keys which have some prefix.
     * 
     * This is useful in cases where you need to process some set of config
     * values (e.g. one setting per data source).
     * 
     * Example:
     * A prefix of 'myfilter' will match 'myfilter.foo' and 'myfilter.foo.bar'.
     */
    Set<String> getConfigKeysWithPrefix(String prefix);

    /**
     * Provides config settings which match the given key pattern.
     * 
     * The pattern is the same pattern used for configuration permissions.
     * 
     * Some examples are:
     * pattern 'a.*' matches 'a.foo' but does not match 'a.foo.bar'
     * pattern 'a.*.*' matches 'a.foo.bar' but does not match 'a.foo'
     * 
     * The key within the returned map is the full key and the value is a 
     * list of the parameters for the key. For example if config has:
     * 'a.foo.bar' and 'a.b.c' and the pattern is 'a.*.*' the resulting map 
     * will contain:
     * 'a.foo.bar': ["foo", "bar"]
     * 'a.b.c': ["a", "b"]
     * 
     * Note that if the config key contains '.' or '\' it will be escaped as 
     * it is in config. For example if config contains 'a.c:\\bar\.jpg' then
     * for the pattern 'a.*' the resulting map would contain:
     * 'a.c:\\bar\.jpg': ["c:\bar.jpg"]
     * The key remains escaped however the paramaters in the list are left unescaped.
     * 
     * @param pattern
     * @return a map of all matching config keys where the key in the map is the config key
     * and the value is a list of all parameters.
     */
    Map<String, List<String>> getConfigKeysMatchingPattern(String pattern);
    
    /**
     * Provides a DocumentTypeConverter, used to convert between filterable document types.
     * 
     * The implementation provided under testing may differ from the implementation provided
     * under Funnelback e.g. during a crawl.
     * 
     * @return
     */
    public FilterDocumentFactory getFilterDocumentFactory();
    
    /**
     * Provides a DocumentTypeFactory, may be used to construct  
     * @return
     */
    public DocumentTypeFactory getDocumentTypeFactory();

    /**
     * Reads a configuration file for the currently running plugin.
     * 
     * The configuration file read is for the currently running plugin and 
     * either the data source or search package the plugin is currently 
     * running on. When a plugin is run during a search, this will be the 
     * results page (profile) the search is running on.  
     * 
     * Plugins can not read the configuration files for other plugins.
     * 
     * @param filename the name of the configuration file to read.
     * @return empty if the file doesn't exist, otherwise the contents of the file.
     */
    public Optional<byte[]> pluginConfigurationFileAsBytes(String filename);

    /**
     * Reads a configuration file for the currently running plugin as a UTF-8 String.
     * 
     * See: {@link #pluginConfigurationFileAsBytes(String)}
     * 
     * @param filename the name of the configuration file to read.
     * @return empty if the file doesn't exist, otherwise the contents of the file.
     */
    public Optional<String> pluginConfigurationFile(String filename);
}
