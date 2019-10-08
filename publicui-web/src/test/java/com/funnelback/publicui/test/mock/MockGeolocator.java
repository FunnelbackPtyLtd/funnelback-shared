package com.funnelback.publicui.test.mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.service.location.Geolocator;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Location;


public class MockGeolocator implements Geolocator{

    public CityResponse cityResponse;
    
    public MockGeolocator(){
        
        Location location = mock(Location.class);
        when(location.getLatitude()).thenReturn(20.0d);
        when(location.getLongitude()).thenReturn(100.0d);
        this.cityResponse = new CityResponse(null, 
            null, 
            null, 
            location, 
            null, 
            null, 
            null,
            null,
            null,
            null);
        
    }
    
    @Override
    public CityResponse geolocate(SearchQuestion searchQuestion) {
        return this.cityResponse;
    }

}
