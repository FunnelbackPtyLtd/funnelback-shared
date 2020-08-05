package com.funnelback.plugin.test;

import org.junit.Test;

import static com.funnelback.plugin.PluginUtil.getCurrentProjectVersion;
import static com.funnelback.plugin.PluginUtil.matchesSemver;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void testPluginDetailsProperties() {
        new CheckPluginDetailsPropertiesHelper().checkPropertiesOk();
    }

    @Test
    public void testVersionIsSemver() {
        String checkVersionHelp = "Check the <version> node of " +
                        "the project pom.xml, it should be of the form MAJOR.MINOR.PATCH, " +
                        "see https://semver.org/ for more details";
        String pluginVersion = getCurrentProjectVersion().orElseThrow(
                () -> new AssertionError(
                        "unable find a version from the plugin pom.xml file. " + checkVersionHelp));
        assertTrue(
                "Plugin version '" + pluginVersion + "' is not a valid semver version; " + checkVersionHelp,
                matchesSemver(pluginVersion));
    }

}
