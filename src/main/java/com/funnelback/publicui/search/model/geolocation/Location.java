package com.funnelback.publicui.search.model.geolocation;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

/**
 * <p>
 * This class represents a location along with a range of 'names' which apply to
 * it, such as countries, cities, postalCodes etc.
 * </p>
 * 
 * <p>
 * The available fields are based closely on the MaxMind2 data.
 * </p>
 * 
 * @since 13.0
 */
@Data
@Builder
@AllArgsConstructor
public class Location {

    /**
     * The city name of the represented location if it can be determined,
     * otherwise null.
     * 
     * @since 13.0
     */
    private final String city;

    /**
     * The two letter country code (ISO 3166-1 alpha code) of the represented location if it can be
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
     * The GeoName ID of the country.
     * 
     * @since 15.24
     */
    private final Integer countryGeoNameId;

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
    private final Double latitude;

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
    private final Double longitude;

    /**
     * The metro code of the represented location if it can be determined,
     * otherwise zero.
     * 
     * @since 13.0
     */
    private final Integer metroCode;

    /**
     * The postal code of the represented location if it can be determined,
     * otherwise null.
     * 
     * @since 13.0
     */
    private final String postalCode;
    
    /**
     * The subdivisions of the location.
     *  
     * This will generally hold the sate or province the location is in, however
     * it depends on on the particular country. Some locations may have multiple
     * subdivisions. The order is from the least specific to most specific division.
     * 
     * 
     * @since 15.24
     */
    private final List<Subdivision> subdivisions;
    
    @AllArgsConstructor
    @Getter
    public static class Subdivision {
        
        /**
         * The GeoName id of this subdivision.
         * 
         * @since 15.24
         */
        private final Integer geoNameId;
        
        /**
         * The name of the subdivision.
         * 
         * @since 15.24
         */
        private final String name;
        
        /**
         * The ISO-3166-2 code of the subdivision.
         * 
         * @since 15.24
         */
        private String isoCode;
        
    }

}
