package ${package};

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.funnelback.plugin.gatherer.PluginGatherContext;
import com.funnelback.plugin.gatherer.PluginGatherer;
import com.funnelback.plugin.gatherer.PluginStore;

public class _ClassNamePrefix_PluginGatherer implements PluginGatherer {

    final PluginUtils pluginUtils = new PluginUtils();
    private static final Logger log = LogManager.getLogger(_ClassNamePrefix_PluginGatherer.class);
    
    @Override
    public void gather(PluginGatherContext pluginGatherContext, PluginStore store) throws Exception {
        log.debug("Gathering documents");
    }

    @Override
    public void gather(PluginGatherContext pluginGatherContext, PluginStore store, VirusScanner virusScanner) throws Exception {
        log.debug("Gathering documents with virus scanner);
    }

}
