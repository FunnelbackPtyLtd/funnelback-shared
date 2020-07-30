package com.funnelback.plugin.test;

import com.funnelback.plugin.PluginDetailsConstants;
import lombok.extern.log4j.Log4j2;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Log4j2
public class CheckPluginDetailsPropertiesHelper {

    public void checkPropertiesOk() {

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(
                    System.getProperty("basedir")  // should be the root directory of the plugin project
                    + "/" + PluginDetailsConstants.PROPERTY_DETAILS_FILE));
        } catch (IOException ioe ) {
            ioe.printStackTrace();
            fail("properties file missing or unreadable - should be present at '"
                    + PluginDetailsConstants.PROPERTY_DETAILS_FILE);
        }

        assertNotNull(
                "should have " + PluginDetailsConstants.NAME + " set",
                properties.get(PluginDetailsConstants.NAME));
        assertNotNull(
                "should have " + PluginDetailsConstants.DESCRIPTION + " set",
                properties.get(PluginDetailsConstants.DESCRIPTION));
        assertTrue(
                "should have one of " + PluginDetailsConstants.RUNS_ON_DATASOURCE
                        + " or "
                        + PluginDetailsConstants.RUNS_ON_RESULTS_PAGE
                        + " set",
                properties.get(PluginDetailsConstants.RUNS_ON_DATASOURCE) != null ||
                        properties.get(PluginDetailsConstants.RUNS_ON_RESULTS_PAGE) != null);
    }

}
