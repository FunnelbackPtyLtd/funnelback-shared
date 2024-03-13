package com.funnelback.mock.helpers;

public interface PluginConfigurationFileSettingMock {

    /**
     * Sets the content of a plugin configuration file on the mock.
     * 
     * @param filename the name of the plugin configuration file e.g. `maps.cfg`
     * @param content plugin configuration data
     */
    void setPlugingConfigurationFileContent(String filename, String content);
    
    /**
     * Sets the content of a plugin configuration file on the mock.
     * 
     * @param filename the name of the plugins configuration file e.g. `maps.cfg`
     * @param content plugin configuration data
     */
    void setPlugingConfigurationFileContentAsBytes(String filename, byte[] content);
}
