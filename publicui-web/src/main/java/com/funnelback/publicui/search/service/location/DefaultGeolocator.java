package com.funnelback.publicui.search.service.location;

import java.io.IOException;

import lombok.extern.log4j.Log4j;

import org.apache.commons.lang.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.web.binding.SearchQuestionBinder;
import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;

/**
 * Provide a service which determines the user's location based on the IP
 * address of the user, using the MaxMind geocoding database.
 */
@Log4j
@Component
public class DefaultGeolocator implements Geolocator {

    @Autowired
    private ConfigRepository configRepository;

    private LookupService lookupService = null;
    private String lookupServiceDatabase = "";

    /**
     * Determine the user's location based on the IP address of the user, using
     * the MaxMind geocoding database.
     */
    @Override
    public Location geolocate(SearchQuestion question) {
        updateLookupServiceIfRequired();

        if (lookupService != null) {
            String remoteIpAddress = question.getRequestIdToLog();
            
            // TODO - FUN-5392 - Just for testing - remove this before shipping.
            if (question.getAdditionalParameters().containsKey("IP_test")) {
                remoteIpAddress = question.getAdditionalParameters().get("IP_test")[0];
            }

            Location result = lookupService.getLocation(remoteIpAddress);
            if (result != null) {
                return result;
            }
        }
        return new Location(); // Provide an unknown location
    }

    /**
     * Create a new lookupService if the requested database in global.cfg has
     * been changed
     */
    private void updateLookupServiceIfRequired() {
        try {
            String newLookupServiceDatabase = configRepository
                    .getGlobalConfiguration().value(Keys.GEOLOCATION_DATABASE_KEY);
            if (!lookupServiceDatabase.equals(newLookupServiceDatabase)) {
                // Recreate the lookup service

                lookupServiceDatabase = newLookupServiceDatabase;
                
                StopWatch s = new StopWatch();
                s.start();
                
                lookupService = new LookupService(lookupServiceDatabase,
                        LookupService.GEOIP_MEMORY_CACHE
                                | LookupService.GEOIP_CHECK_CACHE);
                
                s.stop();
                log.debug("LookupService recreated. Took " + s);
            }
        } catch (IOException e) {
            log.error("Error while trying to create lookupService for "
                    + lookupServiceDatabase, e);
            lookupServiceDatabase = "";
            lookupService = null;
        }
    }
}
