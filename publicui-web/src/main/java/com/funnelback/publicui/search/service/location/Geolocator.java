package com.funnelback.publicui.search.service.location;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.maxmind.geoip.Location;

/**
 * Provide a service which determines the user's location based on information
 * provided within the searchQuestion.
 */
public interface Geolocator {

    /**
     * Determine the user's location based on information provided within the
     * searchQuestion.
     */
    Location geolocate(SearchQuestion searchQuestion);

}
