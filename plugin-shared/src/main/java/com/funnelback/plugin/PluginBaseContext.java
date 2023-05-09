package com.funnelback.plugin;

import java.io.File;

public interface PluginBaseContext {

    /** The home of the Funnelback installation currently being used to perform filtering. */
    File getSearchHome();

    /** The name of the funnelback collection for which filtering is being performed */
    String getCollectionName();

}
