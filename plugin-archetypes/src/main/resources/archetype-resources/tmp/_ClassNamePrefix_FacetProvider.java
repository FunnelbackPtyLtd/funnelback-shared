package __fixed_package__;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.funnelback.plugin.facets.FacetProvider;
import com.funnelback.plugin.index.IndexConfigProviderContext;

public class _ClassNamePrefix_FacetProvider implements FacetProvider {

    private static final Logger log = LogManager.getLogger(_ClassNamePrefix_FacetProvider.class);
    
    @Override
    public String extraFacetedNavigation(IndexConfigProviderContext context) {
        log.debug("Should the plugin provide additional facets?");
        return null;
    }
}
