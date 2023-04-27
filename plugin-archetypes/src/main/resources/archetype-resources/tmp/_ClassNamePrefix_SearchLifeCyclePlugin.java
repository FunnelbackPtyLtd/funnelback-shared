package ${package};

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.funnelback.plugin.SearchLifeCyclePlugin;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.plugin.search.SearchLifeCycleContext;

public class _ClassNamePrefix_SearchLifeCyclePlugin implements SearchLifeCyclePlugin {

    final PluginUtils pluginUtils = new PluginUtils();
    private static final Logger log = LogManager.getLogger(_ClassNamePrefix_SearchLifeCyclePlugin.class);
    
    @Override
    public void preProcess(SearchLifeCycleContext searchLifeCycleContext, SearchTransaction transaction) {
        log.trace("Modify the search, SearchLifeCyclePlugin has other times the search can be modified.");
    }
}
