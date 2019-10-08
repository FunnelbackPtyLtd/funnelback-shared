package com.funnelback.publicui.search.lifecycle.input.processors;

import static com.funnelback.config.keys.Keys.FrontEndKeys;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.geolocation.Location;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.service.location.Geolocator;
import com.maxmind.geoip2.model.CityResponse;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * <p>
 * Populates the searchQuestion with location related information based on
 * information in the request.
 * </p>
 * 
 * @since 12.4
 */
@Log4j2
@Component("geolocationInputProcessor")
public class Geolocation extends AbstractInputProcessor {

    @Autowired
    @Setter Geolocator geolocator;

    /**
     * <p>
     * Populates the 'location' field of searchQuestion and the 'origin' CGI
     * based on information in the request.
     * </p>
     */
    @Override
    public void processInput(SearchTransaction searchTransaction)
        throws InputProcessorException {
        if (SearchTransactionUtils.hasCollection(searchTransaction)) {
            ServiceConfigReadOnly serviceConfigReadOnly = searchTransaction.getQuestion().getCurrentProfileConfig();

            if (serviceConfigReadOnly.get(FrontEndKeys.ModernUi.GeoLocation.ENABLED)) {
                //Sets the location in the data model
                
                CityResponse response = geolocator.geolocate(searchTransaction.getQuestion());
                
                if(response != null) {
                    Location location = new com.funnelback.publicui.search.model.geolocation.Location(response);
                    searchTransaction.getQuestion().setLocation(location);
                    
                    Map<String, String[]> params = searchTransaction.getQuestion().getAdditionalParameters();
                    
                    if (serviceConfigReadOnly.get(FrontEndKeys.ModernUi.GeoLocation.SET_ORIGIN)
                            && !params.containsKey(RequestParameters.ORIGIN)) {
                        // Set the origin CGI parameter for padre only if it is not already set by CGI
                        params.put(RequestParameters.ORIGIN, new String[] { location.getLatitude() + "," + location.getLongitude() });
                    }
                    
                    log.debug("Geocoded location {},{}", location.getLatitude(), location.getLongitude());
                }
            }
        }
    }

}
