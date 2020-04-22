package __fixed_package__;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.funnelback.plugin.gatherer.PluginGatherContext;
import com.funnelback.plugin.gatherer.PluginGatherer;
import com.funnelback.plugin.gatherer.PluginStore;

public class _ClassNamePrefix_PluginGatherer implements PluginGatherer {

    private static final Logger log = LogManager.getLogger(_ClassNamePrefix_PluginGatherer.class);
    
    @Override
    public void gather(PluginGatherContext pluginGatherContext, PluginStore store) throws Exception {
        log.debug("Gathering documents");
    }
}
