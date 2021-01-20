package __fixed_package__;

public class PluginUtils {

    /**
     * The name of the plugin.
     * 
     * Should match the artifactId in the pom.xml file.
     *
     */
    public static final String PLUGIN_NAME = "${artifactId}";
    
    /**
     * A prefix that should be prefixed to configuration keys used by this plugin.
     * 
     * For example if this plugin configured the number of foos to use, the key 
     * might look like:
     * <code>
     * String foosKey = _ClassNamePrefix_PluginUtils.KEY_PREFIX + "foos-count";
     * </code>
     * The resulting key would be:
     * <code>
     * plugin.${artifactId}.config.foos-count
     * </code>
     */
    public static final String KEY_PREFIX = "plugin." + PLUGIN_NAME + ".config." ;
}
