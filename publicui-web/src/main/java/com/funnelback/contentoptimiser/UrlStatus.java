package com.funnelback.contentoptimiser;

import lombok.Getter;
import lombok.Setter;

public class UrlStatus {

		@Getter @Setter private String error;
		// No getter, as this should only be available to admins
		// however, it needs to be here with a setter to allow reading from JSON
		@SuppressWarnings("unused")
		@Setter private String message; 
		@Getter @Setter private String available;
		
		
		public boolean isAvailable() {
			return "true".equals(available);
		} 
		
		
}

