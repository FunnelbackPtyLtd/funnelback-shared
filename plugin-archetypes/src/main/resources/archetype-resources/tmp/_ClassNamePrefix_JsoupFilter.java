package ${package};

import com.funnelback.common.filter.jsoup.FilterContext;
import com.funnelback.common.filter.jsoup.IJSoupFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class _ClassNamePrefix_JsoupFilter implements IJSoupFilter {

    final PluginUtils pluginUtils = new PluginUtils();
    private static final Logger log = LogManager.getLogger(_ClassNamePrefix_JsoupFilter.class);
    
    @Override
    public void processDocument(FilterContext filterContext) {
        log.debug("Processing a document");
    }
}

