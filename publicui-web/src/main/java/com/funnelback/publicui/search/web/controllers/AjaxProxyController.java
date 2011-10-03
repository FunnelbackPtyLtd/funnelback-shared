package com.funnelback.publicui.search.web.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Basic proxy for Ajax services
 */
@CommonsLog
public class AjaxProxyController {

	@RequestMapping("/ajax-proxy.xml")
	public void proxyXml(String url, HttpServletResponse response) throws IllegalStateException, IOException, URISyntaxException {
		if (url == null) {
			response.setContentType("text/xml;charset=UTF-8");
			response.getWriter().write("<error>Missing 'url' parameter</error>");
		} else {
			proxy(url, response);
		}
	}
	
	@RequestMapping("/ajax-proxy.json")
	public void proxyJson(String url, HttpServletResponse response) throws IllegalStateException, IOException, URISyntaxException {
		if (url == null) {
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write("{ \"error\" : \"Missing 'url' parameter\" }");
		} else {
			proxy(url, response);
		}
	}
	
	public void proxy(String url, HttpServletResponse response) throws IllegalStateException, IOException, URISyntaxException {
		log.trace("Proxying URL '" + url + "'");
		URL from = new URL(url);

		// Only supports http scheme for now
		HttpGet httpGet = new HttpGet(
				URIUtils.createURI("http", from.getHost(), from.getPort(), from.getPath(), from.getQuery(), null)		
				);

		HttpClient client = new DefaultHttpClient();
		HttpResponse remoteResponse = client.execute(httpGet);
		response.setContentType(remoteResponse.getEntity().getContentType().getValue());
		for (Header h: remoteResponse.getAllHeaders()) {
			response.addHeader(h.getName(), h.getValue());
		}
				
		IOUtils.copy(remoteResponse.getEntity().getContent(), response.getOutputStream());		
	}
	
}
