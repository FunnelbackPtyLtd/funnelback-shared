package com.funnelback.publicui.test.search.service.log;

import java.io.File;
import java.util.Date;

import com.funnelback.common.config.Files;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.log.ContextualNavigationLog;
import com.funnelback.publicui.search.model.log.FacetedNavigationLog;
import com.funnelback.publicui.search.model.log.Log;

public class LocalLogServiceFNavXmlTests extends AbstractLocalLogServiceXmlTests {
    
    @Override
    protected Log getLog(Collection collection, Date d) {
        return new FacetedNavigationLog(
            d,
            collection,
            p,
            "request-id",
            "userId",
            "facet",
            "query");
    }
    
    @Override
    protected String getLogPrefix() {
        return Files.Log.FACETED_NAVIGATION_LOG_PREFIX;
    }
    
    @Override
    protected String getLogSuffix() {
        return Files.Log.FACETED_NAVIGATION_LOG_EXT;
    }
    
    @Override
    protected void log(Log l) {
        logService.logFacetedNavigation((FacetedNavigationLog) l);
    }
    
    @Override
    protected String getRootTag() {
        return "cfac";
    }
    
    @Override
    protected File getTestResourcesFolder() {
        return new File(TEST_IN_ROOT, "faceted-navigation");
    }

}
