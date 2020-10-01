package com.funnelback.plugin.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.junit.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;

public class CheckPropertiesFileIsValidHelper {

    private static final List<Class<?>> KNOWN_PLUGIN_CLASSES = knownPluginClasses(); 
        
    
    private static List<Class<?>> knownPluginClasses() {
        List<Class<?>> knownPluginClasses = new ArrayList<>();
        knownPluginClasses.add(com.funnelback.plugin.SearchLifeCyclePlugin.class);
        knownPluginClasses.add(com.funnelback.plugin.index.IndexingConfigProvider.class);
        knownPluginClasses.add(com.funnelback.plugin.gatherer.PluginGatherer.class);
        knownPluginClasses.add(com.funnelback.plugin.facets.FacetProvider.class);
        knownPluginClasses.add(com.funnelback.plugin.servlet.filter.ServletFilterHook.class);
        return Collections.unmodifiableList(knownPluginClasses);
    }
    
    
    public void checkPropertiesFileExists(String pluginName, Class<?> testClass) {
        String propsFileName = propsFileName(pluginName);
        Assert.assertTrue("Plugin properties file was not found, does: 'src/main/resources/" + propsFileName + "' exist?\n"
            + "Is the plugin's name: '" + pluginName + "' if not '" + testClass.getName() + "#getPluginName()' may need to be updated.", 
            loadPluginProps(propsFileName).isPresent());
    }
    
    public void checkClassesDefinedInProps(String pluginName) {
        Map<String, String> props = loadPluginProps(propsFileName(pluginName)).orElse(Map.of()); 
        
        for(Class<?> clazz : KNOWN_PLUGIN_CLASSES) {
            String realClass = props.get(clazz.getName());
            if(realClass == null) continue; // not defined in props
            Class<?> pluginClass;
            try {
                pluginClass = this.getClass().getClassLoader().loadClass(realClass);
            } catch (ClassNotFoundException e) {
                Assert.fail("The props file claimed that: '" + clazz.getName() + "' was implemented by '"
                    + realClass + "' however it does not exist.\n"
                        + "Is '" + realClass + "' the correct name?\n"
                        + "Does that class exist?");
                return;
            }
            
            Constructor<?> constructor;
            try {
                 constructor = pluginClass.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                Assert.fail("Could not find a no arg construcor on: '" + realClass + "'.");
                return;
            }
            
            try {
                constructor.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail("Could not create an instance of the plugin class: '" + realClass + "'.\n"
                    + "Does it have a public no args constructor? e.g.: \n"
                    + "    public " + pluginClass.getSimpleName() + "() {\n" 
                    + "        ...\n" 
                    + "    }");
                return;
            }
        }
        
    }
    
    private String propsFileName(String pluginName) {
        return "funnelback-plugin-" + pluginName + ".properties";
    }
    
    private Optional<Map<String, String>> loadPluginProps(String propsFileName) {
        Properties prop = new Properties();
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(propsFileName);
            if(is == null) {
                return Optional.empty();
            }
            
            prop.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        // Don't let people mess with this.
        return Optional.of(Collections.unmodifiableMap((Map) prop));
    }
}
