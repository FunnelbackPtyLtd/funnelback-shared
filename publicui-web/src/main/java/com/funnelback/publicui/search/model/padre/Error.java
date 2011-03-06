package com.funnelback.publicui.search.model.padre;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * PADRE error data
 */
@RequiredArgsConstructor
@ToString
public class Error {

	@Getter final private String userMsg;
	@Getter final private String adminMsg;
	
	/**
	 * Represents XML Schema
	 *
	 */
	public static final class Schema {
		
		public static final String ERROR = "error";
		
		public static final String USERMSG = "usermsg";
		public static final String ADMINMSG = "adminmsg";		
	}
}
	
