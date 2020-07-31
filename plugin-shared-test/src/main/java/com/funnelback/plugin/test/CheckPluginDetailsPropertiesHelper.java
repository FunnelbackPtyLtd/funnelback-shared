package com.funnelback.plugin.test;

import com.funnelback.plugin.PluginDetailsConstants;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Log4j2
public class CheckPluginDetailsPropertiesHelper {

    private static String examplePluginProperties =
                    "# A Human readable plugin name\n" +
                    PluginDetailsConstants.NAME + "=My Plugin Name\n" +
                    "# A Human readable description\n" +
                    PluginDetailsConstants.DESCRIPTION + "=A Plugin Description\n" +
                    "# Whether the plugin should run on datasources - this plugin does\n" +
                    PluginDetailsConstants.RUNS_ON_DATASOURCE + "=true\n" +
                    "# Whether the plugin should run on results pages - this plugin doesn't\n" +
                    PluginDetailsConstants.RUNS_ON_RESULTS_PAGE + "=false\n";

    public void checkPropertiesOk() {
        File expectedPluginDetailsFile =
                new File(Arrays.stream(
                        PluginDetailsConstants.PROPERTY_DETAILS_FILE_PATH).collect(Collectors.joining(File.separator)));

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(expectedPluginDetailsFile));
        } catch (IOException ioe ) {
            ioe.printStackTrace();
            fail("properties file missing or unreadable - should be present at '"
                    + expectedPluginDetailsFile + "\n"
                    + "and look something like\n\n" +
                    examplePluginProperties + "\n\n");
        }

        assertNotNull(
                "property " + PluginDetailsConstants.NAME + " missing.\n" +
                        "Should be set to a human readable name for the plugin, i.e. '"
                        + PluginDetailsConstants.NAME + "=My Plugin Name'\n" +
                " in " + expectedPluginDetailsFile,
                properties.get(PluginDetailsConstants.NAME));
        assertNotNull(
                "property " + PluginDetailsConstants.DESCRIPTION + " missing.\n" +
                        "Should be set to a brief description of the plugin, i.e. '" +
                        PluginDetailsConstants.DESCRIPTION + "=Adds functionality to datasources'\n" +
                        " in " + expectedPluginDetailsFile,

                properties.get(PluginDetailsConstants.DESCRIPTION));
        assertTrue(
                "should have one of " + PluginDetailsConstants.RUNS_ON_DATASOURCE
                        + " or "
                        + PluginDetailsConstants.RUNS_ON_RESULTS_PAGE
                        + " set boolean values, to indicate where the plugin should run.\n" +
                        "e.g. If the plugin should run on datasources, but not on the results page, set \n" +
                        PluginDetailsConstants.RUNS_ON_DATASOURCE + "=true\n" +
                        PluginDetailsConstants.RUNS_ON_DATASOURCE + "=false\n" +
                        " in " + expectedPluginDetailsFile,
                getBooleanPropertyValue(properties, PluginDetailsConstants.RUNS_ON_DATASOURCE) ||
                        getBooleanPropertyValue(properties, PluginDetailsConstants.RUNS_ON_RESULTS_PAGE));
    }

    private Boolean getBooleanPropertyValue(Properties properties, String property) {
        Object propertyValue = properties.get(property);
        return propertyValue == null ? Boolean.FALSE : Boolean.parseBoolean(propertyValue.toString().trim());
    }

}
