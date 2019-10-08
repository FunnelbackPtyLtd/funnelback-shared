package com.funnelback.publicui.search.service.location;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.maxmind.MaxMindCityLoader;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.GeoIp2Provider;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * Provide a service which determines the user's location based on the IP
 * address of the user, using the MaxMind geocoding database.
 */
@Log4j2
@Component
public class DefaultGeolocator implements Geolocator {

    @Autowired
    @Setter
    private ConfigRepository configRepository;
    
    @Autowired @Setter
    private File searchHome;

    // Once set we can never replace it as we don't close it or expose the close method.
    private GeoIp2Provider reader = null;

    @Setter
    private MaxMindCityLoader maxMindCityLoader = new MaxMindCityLoader();
    
    /**
     * Determine the user's location based on the IP address of the user, using
     * the MaxMind geocoding database.
     */
    @Override
    public CityResponse geolocate(SearchQuestion question) {
        loadLookupServiceIfNotLoaded();
        if (reader != null) {
            String remoteIpAddress = question.getRequestId();
            try {
                return reader.city(InetAddress.getByName(remoteIpAddress));
            } catch (IOException | GeoIp2Exception e) {
                log.catching(e);
            }
        }
        return null;
    }
    
    private void loadLookupServiceIfNotLoaded() {
        if(reader == null) {
            _loadLookupService();
        }
        
    }
    private synchronized void _loadLookupService() {
        if(reader != null) return;
        try {
            this.reader = maxMindCityLoader.loadReaderFromSearchHome(searchHome);
        } catch (Exception e) {
            log.catching(e);
            return;
        }
    }
}
