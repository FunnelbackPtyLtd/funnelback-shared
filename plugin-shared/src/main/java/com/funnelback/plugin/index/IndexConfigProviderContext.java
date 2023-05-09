package com.funnelback.plugin.index;

import com.funnelback.plugin.PluginBaseConfigContext;
import com.funnelback.plugin.PluginBaseContext;

import java.util.Optional;

import java.io.IOException;

public interface IndexConfigProviderContext extends PluginBaseContext, PluginBaseConfigContext {
    
    /**
     * When the plugin is called for a profile, this will be set to the profile and view value
     * which is used in the profile parameter in the modern UI. This also matches the profile
     * folder used on disk in the collection configuration.
     * 
     * @return Empty when not running on a profile otherwise the profile and view.
     */
    Optional<String> getProfileWithView();
    
    /**
     * 
     * @return Empty when not running on a profile otherwise the profile this is being run.
     */
    Optional<String> getProfile();

    /**
     * May be used to read a file from the current collection's configuration directory.
     *
     * @param pathsBelowConf The path below the current collection's configuration directory e.g.
     * List.of("collection.cfg") to read $SEARCH_HOME/conf/$COLLECTION/collection.cfg
     * @return if the file is present the bytes of that file, otherwise empty.
     */
    Optional<byte[]> readCollectionConfigFile(String ... pathsBelowConf) throws IOException;
    
}
