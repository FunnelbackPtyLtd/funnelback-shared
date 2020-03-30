package com.funnelback.plugin.test;

import org.junit.Test;

/**
 * An abstract test which most plugins should have.
 * 
 * This will do some checks on the props file in an attempt to tell the user 
 * earlier if something is wrong with the plugin.
 *
 */
public abstract class AbstractPluginPropertiesFileTest {

    public abstract String getPluginName();
    
    @Test
    public void testDefinedClassesInPropsExist() {
        new CheckPropertiesFileIsValidHelper().checkClassesDefinedInProps(getPluginName());
    }
    
    @Test
    public void testPropsFileExist() {
        new CheckPropertiesFileIsValidHelper().checkPropertiesFileExists(getPluginName(), this.getClass());
    }
}
