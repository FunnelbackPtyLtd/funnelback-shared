package com.funnelback.publicui.test.search.web.filters;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.funnelback.config.configtypes.service.DefaultServiceConfig;
import com.funnelback.config.configtypes.service.ServiceConfig;
import com.funnelback.config.data.InMemoryConfigData;
import com.funnelback.config.data.environment.NoConfigEnvironment;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.web.filters.ConfigurableCorsFilter;
import org.mockito.Mockito;

import static com.funnelback.config.keys.Keys.FrontEndKeys;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigurableCorsFilterTest {

    @Test
    public void test() throws Exception {
        ConfigurableCorsFilter configurableCorsFilter = new ConfigurableCorsFilter();

        String collection = "coll";
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getParameter(RequestParameters.COLLECTION)).thenReturn(collection);

        ServiceConfig serviceConfig = new DefaultServiceConfig(new InMemoryConfigData(Maps.newHashMap()), new NoConfigEnvironment());
        serviceConfig.set(FrontEndKeys.ModernUi.CORS_ALLOW_ORIGIN, Optional.of("pa"));

        ConfigRepository configRepository = mock(ConfigRepository.class);
        Collection c = mock(Collection.class);
        Mockito
            .when(configRepository.getServiceConfig(collection,"_default"))
            .thenReturn(serviceConfig);

        when(configRepository.getCollection(collection)).thenReturn(c);
        when(c.getId()).thenReturn(collection);

        configurableCorsFilter.setConfigRepository(configRepository);

        Optional<String> ret = configurableCorsFilter.getCorsAllowOrigin(servletRequest, null);
        Assert.assertTrue(ret.isPresent());
        Assert.assertEquals("pa", ret.get());
    }
}
