package com.funnelback.publicui.search.model.transaction.testutils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.funnelback.mock.helpers.MapBackedConfig;
import com.funnelback.publicui.search.model.collection.ServiceConfig;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Delegate;

public class SearchQuestionTestHelper {

    /**
     * Sets the map as the profile config options to use on the given search question.
     * 
     * The map is not copied so further changes to the map will be reflected in the SearchQuestion.
     * 
     * Example:
     * <code>
     * SearchTransaction st = new SearchTransaction();
     * Map<String, String> profileOptions = new HashMap<>();
     * 
     * SearchQuestionProfileConfigurationHelper.setProfileConfigOptions(profileOptions, st.getQuestion());
     * 
     * profileOptions.put("foo", "bar");
     * 
     * // a will be set to "bar".
     * String a = st.getQuestion().getCurrentProfileConfig().get("foo");
     * </code>
     * 
     * @param profileConfig the profile config options.
     * @param question the SearchQuestion which will have its {@link SearchQuestion#getCurrentProfileConfig()} 
     * return a config which uses the given map. 
     */
    public static void setCurrentProfileConfig(Map<String, String> profileConfig, SearchQuestion question) {
        question.setServiceConfigProvider(q -> new MyServiceConfig(new MapBackedConfig(profileConfig)));
    }
    
    /**
     * Sets a profile config setting on the search question.
     *  
     * @param question
     * @param key
     * @param value
     */
    public static void setProfileConfigSetting(SearchQuestion question, String key, String value) {
        if(question.getCurrentProfileConfig() == null) {
            setCurrentProfileConfig(new HashMap<>(), question);
        }
        if(question.getCurrentProfileConfig() instanceof MyServiceConfig) {
            MyServiceConfig myServiceConfig = (MyServiceConfig) question.getCurrentProfileConfig();
            myServiceConfig.getMapBackedConfig().setConfigSetting(key, value);
        } else {
            throw new IllegalArgumentException("The ServiceConfig returned by getCurrentProfileConfig() is not one "
                + "which can be edited by this method. Perhaps something else has already configured ServiceConfig.");
        }
    }


    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class MyServiceConfig implements ServiceConfig {

        @Getter @Delegate private MapBackedConfig mapBackedConfig;

        @Override
        public String get(String key) {
            return mapBackedConfig.getConfigSetting(key);
        }

        @Override
        public Set<String> getRawKeys() {
            return mapBackedConfig.getConfigKeysWithPrefix("");
        }
    }
}
