package com.funnelback.publicui.test.mock;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.service.location.Geolocator;
import com.maxmind.geoip.Location;


public class MockGeolocator implements Geolocator{

    public Location location;
    
    public MockGeolocator(){
        this.location = new Location();
        this.location.latitude = (float) 20;
        this.location.longitude = (float) 100;
    }
    @Override
    public Location geolocate(SearchQuestion searchQuestion) {
        return this.location;
    }

}
