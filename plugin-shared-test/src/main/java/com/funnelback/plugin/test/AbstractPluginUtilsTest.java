package com.funnelback.plugin.test;

import com.funnelback.plugin.PluginUtilsBase;
import com.funnelback.plugin.details.model.PluginConfigKey;
import com.funnelback.plugin.details.model.PluginConfigKeyAllowedValue;
import com.funnelback.plugin.details.model.PluginConfigKeyType;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractPluginUtilsTest {

    public abstract PluginUtilsBase getPluginUtils();

    @Test
    public void testPluginId(){
        Assertions.assertTrue(StringUtils.isNotBlank(getPluginUtils().getPluginId()));
    }

    @Test
    public void testPluginName(){
        Assertions.assertTrue(StringUtils.isNotBlank(getPluginUtils().getPluginName()));
    }

    @Test
    public void testPluginDescription(){
        Assertions.assertTrue(StringUtils.isNotBlank(getPluginUtils().getPluginDescription()));
    }
    @Test
    public void testPluginTarget(){
        Assertions.assertFalse(getPluginUtils().getPluginTarget().isEmpty(), "At least one plugin target should be defined");
    }

    @Test
    public void testProductSubtopic(){
        Assertions.assertFalse(getPluginUtils().getProductSubtopic().isEmpty(), "At least one product subtopic should be selected");
    }

    @Test
    public void testProductTopic(){
        Assertions.assertFalse(getPluginUtils().getProductTopic().isEmpty(), "At least one product topic should be selected");
    }

    @Test
    public void testAudience(){
        Assertions.assertFalse(getPluginUtils().getAudience().isEmpty(), "At least one audience should be selected");
    }

    @Test
    public void testMarketplaceSubtype(){
        Assertions.assertFalse(getPluginUtils().getMarketplaceSubtype().isEmpty(), "At least one marketplace subtype should be selected");
    }

    @Test
    public void testConfigKeys(){
        Assertions.assertFalse(getPluginUtils().getConfigKeys().isEmpty(), "At least one config key should be defined");
    }

    @Test
    public void testPluginKeyCanHaveRegex(){
        List<PluginConfigKeyType.Format> typesNotAllowedWithRegex = Stream.of(
                        PluginConfigKeyType.Format.ARRAY,
                        PluginConfigKeyType.Format.BOOLEAN,
                        PluginConfigKeyType.Format.METADATA)
                .collect(Collectors.toList());

        getPluginUtils().getConfigKeys().forEach(cfgKey -> {
            if (cfgKey instanceof PluginConfigKey) {
                PluginConfigKey<?> pCfgKey = (PluginConfigKey<?>) cfgKey;
                PluginConfigKeyType.Format keyType = pCfgKey.getType().getType();

                if (typesNotAllowedWithRegex.contains(keyType)) {
                    PluginConfigKeyAllowedValue<?> av = pCfgKey.getAllowedValue();

                    if (av != null) {
                        Assertions.assertThrows(NullPointerException.class, av::getRegex,
                            "Property allowedValue as regex pattern not allowed for: Array, Boolean and Metadata types."); // expecting nullPointer -> regex not defined
                    }
                }
            }
        });
    }
}
