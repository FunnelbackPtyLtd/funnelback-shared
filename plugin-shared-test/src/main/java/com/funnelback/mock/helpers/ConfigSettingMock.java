package com.funnelback.mock.helpers;

public interface ConfigSettingMock {

    /**
     * This method can be used to set collection or profile config options on the mock object.
     * 
     * The mock object itself declares if it is for a profile or collection.
     * 
     * To remove a value set the value to null.
     * 
     * 
     * @param key
     * @param value
     */
    public void setConfigSetting(String key, String value);
}
