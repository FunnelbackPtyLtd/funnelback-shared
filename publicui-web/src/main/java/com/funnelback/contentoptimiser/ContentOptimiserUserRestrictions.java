package com.funnelback.contentoptimiser;

import lombok.Getter;
import lombok.ToString;

import com.funnelback.common.config.Config;

/**
 * This class uses the config to determine whether or not a particular user (admin or non) is able to access the 
 * content optimiser in a particular access mode.  
 * 
 * @author tim
 *
 */
@ToString
public class ContentOptimiserUserRestrictions {
    @Getter private final boolean allowNonAdminTextAccess;
    @Getter private final boolean allowNonAdminFullAccess;
    
    @Getter private final boolean onAdminPort;
    
    
    public ContentOptimiserUserRestrictions(String nonAdminAccess, boolean onAdminPort) {
        this.onAdminPort = onAdminPort;

        // work out what level of access non admins have 
        if (nonAdminAccess != null) {
            if(Config.isTrue(nonAdminAccess)) {
                allowNonAdminTextAccess = true;
                allowNonAdminFullAccess = true;
            } else if("textonly".equals(nonAdminAccess)) {
                allowNonAdminTextAccess = true;
                allowNonAdminFullAccess = false;
            } else {
                allowNonAdminTextAccess = false;
                allowNonAdminFullAccess = false;                
            }
        } else {
            allowNonAdminTextAccess = false;
            allowNonAdminFullAccess = false;                
        }
    }

}
