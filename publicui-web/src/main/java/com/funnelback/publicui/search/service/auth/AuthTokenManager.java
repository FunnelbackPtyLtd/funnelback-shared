package com.funnelback.publicui.search.service.auth;

public interface AuthTokenManager {

	public String getToken(String url, String serverSecret);
	
	public boolean checkToken(String token, String url, String serverSecret);

}
