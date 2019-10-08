package com.funnelback.publicui.search.service.location;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.maxmind.geoip2.model.CityResponse;

/**
 * Provide a service which determines the user's location based on information
 * provided within the searchQuestion.
 */
public interface Geolocator {

    /**
     * Determine the user's location based on information provided within the
     * searchQuestion.
     */
    CityResponse geolocate(SearchQuestion searchQuestion);

}
