package com.funnelback.publicui.search.service.location;

import java.util.Optional;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.http.conn.util.InetAddressUtils;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.common.maxmind.MaxMindCityLoader;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.service.ConfigRepository;
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
            Optional<InetAddress> address = makeAddress(remoteIpAddress);
            if(address.isPresent()) {
                try {
                    return reader.city(address.get());
                } catch (IOException | GeoIp2Exception e) {
                    log.catching(e);
                }
            }
        }
        return null;
    }
    
    Optional<InetAddress> makeAddress(String possibleIp) {
        if(possibleIp == null) return Optional.empty();
        if(InetAddressUtils.isIPv4Address(possibleIp) || InetAddressUtils.isIPv6Address(possibleIp)) {
             try {
                return Optional.of(InetAddress.getByName(possibleIp));
            } catch (UnknownHostException e) {
                log.debug("Could not make InetAddress from '{}'", possibleIp, e);
                return Optional.empty();
            }
        } else {
            log.debug("Can not make a InetAddress from '{}' which does not look like an IP address", possibleIp);
            return Optional.empty();
        }
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
