package com.funnelback.plugin.index;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.config.keys.WildCardKeyMatcher;

public interface IndexConfigProviderContext {

    /** The home of the Funnelback installation currently being used to perform filtering. */
    public File getSearchHome();

    /** The name of the funnelback collection for which filtering is being performed */
    public String getCollectionName();
    
    /**
     * TODO
     * matches the folder name used on disk
     * 
     * also matches the name used in the profile param in the modern UI
     * @return
     */
    public Optional<String> getProfileWithView();
    
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
     * TODO
     */
    public Optional<byte[]> readCollectionConfigFile(String ... pathsBelowConf) throws IOException;
    
    
}
