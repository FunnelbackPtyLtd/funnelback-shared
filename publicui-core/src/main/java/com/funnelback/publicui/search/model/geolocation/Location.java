package com.funnelback.publicui.search.model.geolocation;

import lombok.Data;

/**
 * <p>
 * This class represents a location along with a range of 'names' which apply to
 * it, such as countries, cities, postalCodes etc.
 * </p>
 * 
 * <p>
 * The available fields are based closely on the MaxMind Location object, which
 * we wrap to provide getters and setters for the fields (allowing access to
 * them from FreeMarker).
 * </p>
 * 
 * @since 13.0
 */
@Data
public class Location {

    /**
     * The area code of the represented location if it can be determined,
     * otherwise zero.
     * 
     * @since 13.0
     */
    private final int areaCode;

    /**
     * The city name of the represented location if it can be determined,
     * otherwise null.
     * 
     * @since 13.0
     */
    private final String city;

    /**
     * The two letter country code of the represented location if it can be
     * determined, otherwise null.
     * 
     * @since 13.0
     */
    private final String countryCode;

    /**
     * The full country name of the represented location if it can be
     * determined, otherwise null.
     * 
     * @since 13.0
     */
    private final String countryName;

    /**
     * The designated market area code of the represented location if it can be
     * determined, otherwise zero.
     * 
     * @since 13.0
     */
    private final int dmaCode;

    /**
     * <p>
     * The latitude of the represented location.
     * </p>
     * 
     * <p>
     * Defaults to zero if it cannot be determined even though that represents a
     * legitimate location.
     * </p>
     * 
     * @since 13.0
     */
    private final float latitude;

    /**
     * <p>
     * The longitude of the represented location.
     * </p>
     * 
     * <p>
     * Defaults to zero if it cannot be determined even though that represents a
     * legitimate location.
     * </p>
     * 
     * @since 13.0
     */
    private final float longitude;

    /**
     * The metro code of the represented location if it can be determined,
     * otherwise zero.
     * 
     * @since 13.0
     */
    private final int metroCode;

    /**
     * The postal code of the represented location if it can be determined,
     * otherwise null.
     * 
     * @since 13.0
     */
    private final String postalCode;

    /**
     * The region name of the represented location if it can be determined,
     * otherwise null.
     * 
     * @since 13.0
     */
    private final String region;

    /**
     * Construct a new Location object based on a MaxMind Location object (which
     * is generally produced based on the source user's IP address).
     * 
     * @since 13.0
     */
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
