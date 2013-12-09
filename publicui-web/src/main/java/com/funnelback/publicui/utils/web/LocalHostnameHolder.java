package com.funnelback.publicui.utils.web;

import java.net.InetAddress;
import java.net.UnknownHostException;

import lombok.Getter;
import lombok.extern.log4j.Log4j;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.stereotype.Component;

/**
 * Will hold the local hostname, determined at startup time.
 */
@Log4j
@Component
public class LocalHostnameHolder {

    /** Various forms of localhost */
    public static final String[] LOCALHOST_NAMES = new String[] {
        "localhost", "127.0.0.1", "::1"
    };
    
    @Getter private String hostname = null;

    public LocalHostnameHolder(String hostname) {
        this.hostname = hostname;
    }
    
    public LocalHostnameHolder() {
        try {
            hostname = InetAddress.getLocalHost().getHostName();
            log.info("Local hostname detected as '" + hostname + "'");
        } catch (UnknownHostException uhe) {
            log.error("Could not determine local hostname. "
                    + "Search logs will not use the hostname in their filename", uhe);
        }
    }
    
    /**
     * @return whether the hostname points to 'localhost' or not.
     */
    public boolean isLocalhost() {
        return ArrayUtils.contains(LOCALHOST_NAMES, hostname);
    }
    
    public String getShortHostname() {
        if (hostname != null) {
            return hostname.replaceAll("\\..*$", "");
        } else {
            return null;
        }
    }

    
}
