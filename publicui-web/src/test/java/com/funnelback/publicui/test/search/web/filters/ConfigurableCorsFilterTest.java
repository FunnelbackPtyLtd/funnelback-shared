package com.funnelback.publicui.test.search.web.filters;

import java.util.Optional;

import javax.servlet.ServletRequest;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.web.filters.ConfigurableCorsFilter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigurableCorsFilterTest {

    @Test
    public void test() throws Exception {
        ConfigurableCorsFilter configurableCorsFilter = new ConfigurableCorsFilter();
        
        String collection = "coll";
        ServletRequest servletRequest = mock(ServletRequest.class);
        when(servletRequest.getParameter(RequestParameters.COLLECTION)).thenReturn(collection);
        
        ConfigRepository configRepository = mock(ConfigRepository.class);
        Collection c = mock(Collection.class);
        when(configRepository.getCollection(collection)).thenReturn(c);
        
        Config config = mock(Config.class);
        when(config.value(Keys.ModernUI.CORS_ALLOW_ORIGIN)).thenReturn("pa");
        when(c.getConfiguration()).thenReturn(config);
        
        configurableCorsFilter.setConfigRepository(configRepository);
        
        Optional<String> ret = configurableCorsFilter.getCorsAllowOrigin(servletRequest, null);
        Assert.assertTrue(ret.isPresent());
        Assert.assertEquals("pa", ret.get());
        
    }
}
