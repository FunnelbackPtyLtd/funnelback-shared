package com.funnelback.publicui.test.search.service.log;

import java.io.File;
import java.util.Arrays;
import java.util.Date;

import com.funnelback.common.config.Files;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.log.ContextualNavigationLog;
import com.funnelback.publicui.search.model.log.Log;

public class LocalLogServiceCNavXmlNonAsciiTests extends AbstractLocalLogServiceXmlTests {
    
    @Override
    protected Log getLog(Collection collection, Date d) {
        return new ContextualNavigationLog(
            d,
            collection,
            p,
            "userId",
            "déclaration fiscale",
            Arrays.asList(new String[] {"déclaration"}),
            null);
    }
    
    @Override
    protected String getLogPrefix() {
        return Files.Log.CONTEXTUAL_NAVIGATION_LOG_PREFIX;
    }
    
    @Override
    protected String getLogSuffix() {
        return Files.Log.CONTEXTUAL_NAVIGATION_LOG_EXT;
    }
    
    @Override
    protected void log(Log l) {
        logService.logContextualNavigation((ContextualNavigationLog) l);
    }
    
    @Override
    protected String getRootTag() {
        return "cflus";
    }
    
    @Override
    protected File getTestResourcesFolder() {
        return new File(TEST_IN_ROOT, "contextual-navigation-non-ascii");
    }

}
