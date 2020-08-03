package com.funnelback.plugin;

/**
 * Constants for plugin details; extra information about the plugin
 * that is stored in a properties file, and exposed in the admin api
 * GET /plugins endpoint
 */
public interface PluginDetailsConstants {
    // Path to the plugin details property file
    String[] PROPERTY_DETAILS_FILE_PATH = new String[] { "docs","plugin-details.properties" };

    // Property key for human readable name for the plugin
    String NAME = "name";
    // Property key for brief description of the plugin
    String DESCRIPTION = "description";

    // For the following property keys, at least one must be set to
    // true for the plugin to be considered valid.

    // Property key for whether the plugin runs on datasources
    String RUNS_ON_DATASOURCE = "runs-on.datasource";
    // Property key for whether the plugin runs on result pages
    String RUNS_ON_RESULT_PAGE = "runs-on.result-page";
}
