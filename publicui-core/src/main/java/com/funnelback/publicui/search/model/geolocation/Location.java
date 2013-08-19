package com.funnelback.publicui.search.model.geolocation;

import lombok.Data;

@Data
public class Location {

    private final int areaCode;
    private final String city;
    private final String countryCode;
    private final String countryName;
    private final int dmaCode;
    private final float latitude;
    private final float longitude;
    private final int metroCode;
    private final String postalCode;
    private final String region;

    public Location(com.maxmind.geoip.Location location) {
        this.areaCode = location.area_code;
        this.city = location.city;
        this.countryCode = location.countryCode;
        this.countryName = location.countryName;
        this.dmaCode = location.dma_code;
        this.latitude = location.latitude;
        this.longitude = location.longitude;
        this.metroCode = location.metro_code;
        this.postalCode = location.postalCode;
        this.region = location.region;
    }

}
