package com.funnelback.publicui.search.service.location;

import org.junit.Assert;
import org.junit.Test;

public class DefaultGeolocatorTest {

    @Test
    public void testMakeAddress() {
        DefaultGeolocator geoLocator = new DefaultGeolocator();
        
        Assert.assertTrue(geoLocator.makeAddress(null).isEmpty());
        Assert.assertTrue("we don't support resolving a hostname, at least we never intended to", geoLocator.makeAddress("funnelback.com").isEmpty());
        Assert.assertTrue(geoLocator.makeAddress("12.12.no.pe").isEmpty());
        Assert.assertTrue(geoLocator.makeAddress("12.12.12.12").isPresent());
        Assert.assertTrue(geoLocator.makeAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334").isPresent());
    }
}
