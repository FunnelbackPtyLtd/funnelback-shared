package __fixed_package__;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.funnelback.filter.api.FilterContext;
import com.funnelback.filter.api.FilterResult;
import com.funnelback.filter.api.documents.NoContentDocument;
import com.funnelback.filter.api.documents.StringDocument;
import com.funnelback.filter.api.filters.PreFilterCheck;
import com.funnelback.filter.api.filters.StringDocumentFilter;

public class _ClassNamePrefix_StringFilter implements StringDocumentFilter {

    private static final Logger log = LogManager.getLogger(_ClassNamePrefix_StringFilter.class);
    
    @Override
    public PreFilterCheck canFilter(NoContentDocument noContentDocument, FilterContext filterContext) {
        return PreFilterCheck.ATTEMPT_FILTER;
    }

    @Override
    public FilterResult filterAsStringDocument(StringDocument stringDocument, FilterContext filterContext) {
        log.trace("Return the given document making no changes.");
        return FilterResult.of(stringDocument);
    }
}
