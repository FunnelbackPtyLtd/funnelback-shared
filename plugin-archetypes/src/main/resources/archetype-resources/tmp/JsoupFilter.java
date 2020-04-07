package ${package};

import com.funnelback.common.filter.jsoup.FilterContext;
import com.funnelback.common.filter.jsoup.IJSoupFilter;

/**
 * Enabled this by adding it to the Jsoup filter chain as well as enabling the plugin.
 * 
 * Example shows reading from configuration and setting metadata.
 */
public class _ClassNamePrefix_JsoupFilter implements IJSoupFilter {

    @Override
    public void processDocument(FilterContext filterContext) {
        // Get the configured class to count from collection.cfg
        String classToCount = filterContext.getSetup().getConfigSetting("myplugin.class-to-count");
        
        // Find the number of elements with that class.
        int elementCount = filterContext.getDocument().getElementsByClass(classToCount).size();
        
        // Add the count to metadata which can be mapped to a metadata class to be used in searches. 
        filterContext.getAdditionalMetadata().put("my-class-count", Integer.toString(elementCount));
    }

}
