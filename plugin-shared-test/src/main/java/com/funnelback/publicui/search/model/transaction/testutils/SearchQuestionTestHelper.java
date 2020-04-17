package com.funnelback.publicui.search.model.transaction.testutils;

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
    public static void setProfileConfigOptions(Map<String, String> profileConfig, SearchQuestion question) {
        question.setServiceConfigProvider(q -> new MyServiceConfig(new MapBackedConfig(profileConfig)));
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
