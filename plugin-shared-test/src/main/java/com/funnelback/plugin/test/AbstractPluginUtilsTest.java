package com.funnelback.plugin.test;

import com.funnelback.plugin.PluginUtilsBase;
import com.funnelback.plugin.details.model.PluginConfigKey;
import com.funnelback.plugin.details.model.PluginConfigKeyAllowedValue;
import com.funnelback.plugin.details.model.PluginConfigKeyType;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractPluginUtilsTest {

    public abstract PluginUtilsBase getPluginUtils();

    @Test
    public void testPluginId(){
        Assert.assertTrue(StringUtils.isNotBlank(getPluginUtils().getPluginId()));
    }

    @Test
    public void testPluginName(){
        Assert.assertTrue(StringUtils.isNotBlank(getPluginUtils().getPluginName()));
    }

    @Test
    public void testPluginDescription(){
        Assert.assertTrue(StringUtils.isNotBlank(getPluginUtils().getPluginDescription()));
    }
    @Test
    public void testPluginTarget(){
        Assert.assertTrue("At least one plugin target should be defined",getPluginUtils().getPluginTarget().size() > 0);
    }

    @Test
    public void testProductSubtopic(){
        Assert.assertTrue("At least one product subtopic should be selected", getPluginUtils().getProductSubtopic().size() > 0);
    }

    @Test
    public void testProductTopic(){
        Assert.assertTrue("At least one product topic should be selected", getPluginUtils().getProductTopic().size() > 0);
    }

    @Test
    public void testAudience(){
        Assert.assertTrue("At least one audience should be selected", getPluginUtils().getAudience().size() > 0);
    }

    @Test
    public void testMarketplaceSubtype(){
        Assert.assertTrue("At least one marketplace subtype should be selected", getPluginUtils().getMarketplaceSubtype().size() > 0);
    }

    @Test
    public void testConfigKeys(){
        Assert.assertTrue("At least one config key should be defined", getPluginUtils().getConfigKeys().size() > 0);
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
                PluginConfigKey pCfgKey = (PluginConfigKey) cfgKey;
                PluginConfigKeyType.Format keyType = pCfgKey.getType().getType();

                if (typesNotAllowedWithRegex.contains(keyType)) {
                    PluginConfigKeyAllowedValue av = pCfgKey.getAllowedValue();

                    if (av != null) {
                        Assert.assertThrows("Property allowedValue as regex pattern not allowed for: Array, Boolean and Metadata types.",
                                NullPointerException.class, () -> av.getRegex()); // expecting nullPointer -> regex not defined
                    }
                }
            }
        });
    }
}
