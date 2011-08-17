package com.funnelback.contentoptimiser;

import lombok.Getter;

import com.funnelback.common.config.Config;

public class ContentOptimiserUserRestrictions {
	@Getter private final boolean allowNonAdminTextAccess;
	@Getter private final boolean allowNonAdminFullAccess;
	
	public ContentOptimiserUserRestrictions(String nonAdminAccess) {
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
