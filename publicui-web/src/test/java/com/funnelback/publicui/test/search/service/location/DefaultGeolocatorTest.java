package com.funnelback.publicui.test.search.service.location;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.common.maxmind.MaxMindCityLoader;
import com.funnelback.common.maxmind.WrappedMaxmindDBProvider;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.service.location.DefaultGeolocator;
import com.maxmind.geoip2.model.CityResponse;

public class DefaultGeolocatorTest {
    
    @Test
    public void testLoadsOnce() throws Exception {
        DefaultGeolocator geoLocator = new DefaultGeolocator();
        MaxMindCityLoader loader = mock(MaxMindCityLoader.class, RETURNS_DEEP_STUBS);
        geoLocator.setMaxMindCityLoader(loader);
        
        WrappedMaxmindDBProvider reader = mock(WrappedMaxmindDBProvider.class);
        when(loader.loadReaderFromSearchHome(any())).thenReturn(reader);
        
        CityResponse response = new CityResponse(null, null, null, null, null, null, null, null, null, null);
        when(reader.city(any())).thenReturn(response);
        
        SearchQuestion question = new SearchQuestion();
        question.setRequestId("127.0.0.1");
        
        Assert.assertNotNull(geoLocator.geolocate(question));
        Assert.assertNotNull(geoLocator.geolocate(question));
        
        verify(loader, times(1)).loadReaderFromSearchHome(any());
    }
}
