package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.Map;

import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.service.location.Geolocator;

import static com.funnelback.config.keys.Keys.FrontEndKeys;

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
                com.maxmind.geoip.Location location = geolocator.geolocate(searchTransaction
                        .getQuestion());
                searchTransaction.getQuestion().setLocation(new com.funnelback.publicui.search.model.geolocation.Location(location));
                
                Map<String, String[]> params = searchTransaction.getQuestion().getAdditionalParameters();
                
                if (serviceConfigReadOnly.get(FrontEndKeys.ModernUi.GeoLocation.SET_ORIGIN)
                        && !params.containsKey(RequestParameters.ORIGIN)) {
                    // Set the origin CGI parameter for padre only if it is not already set by CGI
                    params.put(RequestParameters.ORIGIN, new String[] { location.latitude + ","
                            + location.longitude });
                }
                
                log.debug("Geocoded location " + location.latitude + ","
                        + location.longitude);
            }
        }
    }

}
