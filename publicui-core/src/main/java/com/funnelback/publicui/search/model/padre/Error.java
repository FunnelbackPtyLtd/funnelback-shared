package com.funnelback.publicui.search.model.padre;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * PADRE error data.
 * 
 * @since 11.0
 */
@RequiredArgsConstructor
@ToString
public class Error {

	/** User-friendly message. */
	@Getter final private String userMsg;
	
	/** Technical message for the administrator. */
	@Getter final private String adminMsg;
	
	/** Constants for the PADRE XML result packet tags. */
	public static final class Schema {
		public static final String ERROR = "error";
		
		public static final String USERMSG = "usermsg";
		public static final String ADMINMSG = "adminmsg";		
	}
}
	
