package com.funnelback.plugin.index;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import java.io.File;
import java.io.IOException;

public interface IndexConfigProviderContext {

    /** The home of the Funnelback installation currently being used to perform filtering. */
    public File getSearchHome();

    /** The name of the funnelback collection for which filtering is being performed */
    public String getCollectionName();
    
    /**
     * When the plugin is called for a profile, this will be set to the profile and view value
     * which is used in the profile parameter in the modern UI. This also matches the profile
     * folder used on disk in the collection configuration.
     * 
     * @return Empty when not running on a profile otherwise the profile and view.
     */
    public Optional<String> getProfileWithView();
    
    /**
     * 
     * @return Empty when not running on a profile otherwise the profile this is being run.
     */
    public Optional<String> getProfile();

    /**
     * Provides access to config settings from the current funnelback installation and collection.
     * 
     * Need to set a default value from a Groovy implementation?
     * Consider http://www.groovy-lang.org/operators.html#_elvis_operator
     * 
     * @param key The name of the collection.cfg or global.cfg setting being requested
     * @return The value as set for the current install and collection context
     */
    public String getConfigSetting(String key);
    
    /**
     * Provides a list of config setting keys which have some prefix.
     * 
     * This is useful in cases where you need to process some set of config
     * values (e.g. one setting per data source).
     */
    public Set<String> getConfigKeysWithPrefix(String prefix);
    
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
    public Map<String, List<String>> getConfigKeysMatchingPattern(String pattern);
    
    /**
     * May be used to read a file from the current collection's configuration directory.
     * 
     * @param pathsBelowConf The path below the current collection's configuration directory e.g.
     * List.of("collection.cfg") to read $SEARCH_HOME/conf/$COLLECTION/collection.cfg 
     * @return if the file is present the bytes of that file, otherwise empty.
     * @throws IOException
     */
    public Optional<byte[]> readCollectionConfigFile(String ... pathsBelowConf) throws IOException;
    
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
