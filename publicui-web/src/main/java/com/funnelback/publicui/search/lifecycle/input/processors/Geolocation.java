package com.funnelback.publicui.search.lifecycle.input.processors;

import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.service.location.Geolocator;
import com.maxmind.geoip.Location;

/**
 * <p>
 * Populates location information in the searchQuestion based on information in
 * the request.
 * </p>
 * 
 * @since 12.4
 */
@Log4j
@Component("geolocationInputProcessor")
public class Geolocation implements InputProcessor {

    private final static String GEOLOCATION_ENABLED_CONFIG_KEY = "ui.modern.geolocation.enabled";

    @Autowired
    Geolocator geolocator;

    @Override
    public void processInput(SearchTransaction searchTransaction)
            throws InputProcessorException {
        if (SearchTransactionUtils.hasQuestion(searchTransaction)) {
            if (searchTransaction.getQuestion().getCollection()
                    .getConfiguration()
                    .valueAsBoolean(GEOLOCATION_ENABLED_CONFIG_KEY)) {
                Location location = geolocator.geolocate(searchTransaction
                        .getQuestion());
                searchTransaction.getQuestion().setLocation(location);

                log.debug("Geocoded location " + location.latitude + ","
                        + location.longitude);
            }
        }
    }

}
