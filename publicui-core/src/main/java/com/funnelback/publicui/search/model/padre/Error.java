package com.funnelback.publicui.search.model.padre;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * PADRE error data.
 * 
 * @since 11.0
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Error {

	/** User-friendly message. */
	@Getter private String userMsg;
	
	/** Technical message for the administrator. */
	@Getter private String adminMsg;
	
	/** Constants for the PADRE XML result packet tags. */
	public static final class Schema {
		public static final String ERROR = "error";
		
		public static final String USERMSG = "usermsg";
		public static final String ADMINMSG = "adminmsg";		
	}
}
	
