package ${package};

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.funnelback.plugin.starturl.StartUrlProvider;
import com.funnelback.plugin.starturl.StartUrlProviderContext;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class _ClassNamePrefix_StartUrlProviderPlugin implements StartUrlProvider {

    final PluginUtils pluginUtils = new PluginUtils();
    private static final Logger log = LogManager.getLogger(_ClassNamePrefix_StartUrlProviderPlugin.class);
    
    @Override
    public List<URL> extraStartUrls(StartUrlProviderContext context) {
        /*
            The code below illustrates how to access config settings, config file for the plugin.
            This code needs to be changed/removed as per requirement basis.
        */

        String listKeyValue = context.getConfigSetting(pluginUtils.LIST_KEY.getKey());
        Optional <String> rules = context.pluginConfigurationFile(pluginUtils.RULES_FILE.getName());
        log.debug("Config value: " + listKeyValue);

        return Collections.emptyList();
    }
}
