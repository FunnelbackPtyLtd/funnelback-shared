package com.funnelback.publicui.search.service.auth;


/**
 * AuthToken manager provides and checks the authorisation token used for redirects in click tracking
 * 
 * @author tjones
 * @since v12.4
 */
public interface AuthTokenManager {

	/**
	 * Get a token for redirection to a particular URL and server secret
	 * 
	 * @param url The URL this token will allow redirects to
	 * @param serverSecret The server secret from collection.cfg
	 * @return The auth token to be used with this URL
	 */
	public String getToken(String url, String serverSecret);
	
	/**
	 * Check a redirect token for a particular url
	 * 
	 * @param token The auth token associated with the URL
	 * @param url The URL to redirect to
	 * @param serverSecret The server secret from collection.cfg
	 * @return true if this token matches this URL, false otherwise
	 */
	public boolean checkToken(String token, String url, String serverSecret);

}
