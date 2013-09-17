package com.funnelback.publicui.test.search.service.log;

import java.io.File;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Files;
import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.log.PublicUIWarningLog;

public class LocalLogServicePublicWarningsTest extends AbstractLocalLogServiceTests {

    private File publicUiWarningLogFile = new File(TEST_OUT_ROOT + File.separator + DefaultValues.FOLDER_LOG,
        Files.Log.PUBLIC_UI_WARNINGS_FILENAME);

    @Override
    protected void before() {
    }
    
    @Test
    public void testLogPublicUIWarning() throws Exception {
        NoOptionsConfig config = new NoOptionsConfig(TEST_OUT_ROOT, COLLECTION_NAME);
        Collection c = new Collection(COLLECTION_NAME, config);

        Date now = new Date();
        PublicUIWarningLog warning = new PublicUIWarningLog(now,
                c,
                null,
                null,
                "Test message",
                null);
        
        logService.setSearchHome(TEST_OUT_ROOT);
        logService.logPublicUIWarning(warning);
        
        String actual = FileUtils.readFileToString(publicUiWarningLogFile);
        String expected = PublicUIWarningLog.DATE_FORMAT.format(now) + " " + c.getId() + " - Test message\n";
        
        Assert.assertEquals(expected, actual);
        
        // Append another message
        now = new Date();
        warning = new PublicUIWarningLog(now,
                c,
                null,
                null,
                "Second message",
                null);
        
        logService.logPublicUIWarning(warning);
        
        actual = FileUtils.readFileToString(publicUiWarningLogFile);
        expected += PublicUIWarningLog.DATE_FORMAT.format(now) + " " + c.getId() + " - Second message\n";
        
        Assert.assertEquals(expected, actual);
        
    }

}
