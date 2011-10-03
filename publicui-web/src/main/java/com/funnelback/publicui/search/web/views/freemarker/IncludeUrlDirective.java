package com.funnelback.publicui.search.web.views.freemarker;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.apachecommons.CommonsLog;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import com.funnelback.publicui.i18n.I18n;

import freemarker.core.Environment;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;

/**
 * Includes an external URL.
 * Replacement for NickScript's IncludeUrl plugin
 * Parameters:
 * - url: Absolute URL to include
 * - expiry: Cache TTL, in seconds
 * - start: Regex pattern to consider to start including content
 * - end: Regex pattern to consider to end including content
 * - username: Username if the remote resource require authentication
 * - password: Password if the remote resource require authentication
 * - useragent: Override default user agent
 * - timeout: Time to wait in seconds for the remote resource
 * - convertrelative: wether we should attempt to convert relative links to absolute ones.
 */
@CommonsLog
public class IncludeUrlDirective implements TemplateDirectiveModel {

	public static final String NAME = "IncludeUrl";
	
	public static final int DEFAULT_EXPIRY = 3600; 
	
	private enum Parameters {
		url, expiry, start, end, username, password, useragent, timeout, convertrelative
	}
	
	private CacheManager appCacheManager;
	private I18n i18n;
	
	public IncludeUrlDirective(CacheManager appCacheManager, I18n i18n) {
		this.appCacheManager = appCacheManager;
		this.i18n = i18n;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
			throws TemplateException, IOException {

		TemplateModel param = (TemplateModel) params.get(Parameters.url.toString());
		if (param == null) {
			env.getOut().write("<!-- " + i18n.tr("parameter.missing", "url") + " -->");
			return;
		}
		
		String url = ((TemplateScalarModel) param).getAsString();
		Cache cache = appCacheManager.getCache(NAME);
			
		Element elt = cache.get(url);
		if (elt != null) {
			log.debug("URL '" + url + "' found in cache");
				
			int expiry = DEFAULT_EXPIRY;
			param = (TemplateModel) params.get(Parameters.expiry.toString());
			if (param != null) {
				expiry = ((TemplateNumberModel) param).getAsNumber().intValue();
			}
			
			if ((elt.getLastUpdateTime() + (expiry*1000)) < System.currentTimeMillis()) {
				log.debug("Cache for URL '" + url + "' expired. Requesting content again");
				try {
					String content = getContent(env, url, params);
					if (content != null) {
						cache.remove(elt);
						elt = new Element(url, content);
						cache.put(elt);
						log.debug("Updated cached version of content for URL '" + url + "'");
						
						env.getOut().write(content);
					} else {
						// Unable to refresh content, return previous cached version
						env.getOut().write((String) elt.getObjectValue());
					}
				} catch (Exception e) {
					env.getOut().write("<!-- " + i18n.tr("freemarker.method.IncludeUrlDirective.refresh.error") + " -->");
					log.error("Error while requesting request content from url '" + url + "'. Previous cached version will be returned.", e);
					env.getOut().write((String) elt.getObjectValue());
				}
			} else {
				// Cache not expired
				log.debug("Returned cached version for URL '" + url + "'");
				String content = (String) elt.getObjectValue();
				env.getOut().write(content);
			}
			
		} else {
			log.debug("URL '" + url + "' not found in cache");
			
			try {
				String content = getContent(env, url, params);
				if (content != null) {
					elt = new Element(url, content);
					elt.setEternal(true);
					cache.put(elt);
					
					env.getOut().write(content);
				} else {
					env.getOut().write("<!-- No remote content returned -->");
				}
			} catch (Exception e) {
				env.getOut().write("<!-- " + i18n.tr("freemarker.method.IncludeUrlDirective.remote.error") + " -->");
				log.error("Error while requesting request content from URL '" + url + "'", e);
			}
		}
	}
	
	/**
	 * Request remote content.
	 * @param env Template environment
	 * @param url Remote URL
	 * @param params Template params
	 * @return The remote content, or null if there was a problem in the question parameters
	 * @throws TemplateModelException
	 * @throws IOException
	 * @throws AuthenticationException
	 */
	private String getContent(Environment env, String url, Map<String, TemplateModel> params) throws TemplateModelException, IOException, AuthenticationException {
		
		log.debug("Initiating new HTTP request to '" + url + "'");
		HttpClient httpClient = new DefaultHttpClient();
		HttpParams httpParams = httpClient.getParams();
		
		HttpGet httpGet = new HttpGet(url);
		
		TemplateModel param = params.get(Parameters.timeout.toString());
		if (param != null) {
			int timeout = ((TemplateNumberModel) param).getAsNumber().intValue();
			HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
			HttpConnectionParams.setSoTimeout(httpParams, timeout);

			log.debug("Set timeout to '" + timeout + "'");
		}
		
		param = params.get(Parameters.useragent.toString());
		if (param != null) {
			String ua = ((TemplateScalarModel) param).getAsString();
			HttpProtocolParams.setUserAgent(httpParams, ua);
			
			log.debug("Set user agent to '" + ua + "'");
		}
		
		param = params.get(Parameters.username.toString());
		if (param != null) {
			String userName = ((TemplateScalarModel) param).getAsString();
			
			param = params.get(Parameters.password.toString());
			if (param != null) {
				String password = ((TemplateScalarModel) param).getAsString();
				UsernamePasswordCredentials creds = new UsernamePasswordCredentials(userName, password);
				httpGet.addHeader(new BasicScheme().authenticate(creds, httpGet));
				
				log.debug("Set credentials with username '" + userName + "'");
				
			} else {
				env.getOut().write("<!-- " + i18n.tr("freemarker.method.IncludeUrlDirective.remote.password") + " -->");
				return null;
			}
		}
		
		HttpResponse response = httpClient.execute(httpGet);
		if(response.getEntity() != null) {
			InputStream is = response.getEntity().getContent();
			try {
				return transformContent(url, IOUtils.toString(is), params);
			} finally {
				IOUtils.closeQuietly(is);
			}
		}
				
		return null;
	}
	
	/**
	 * Transforms content if required: Extracts the relevant part and convert relative links
	 * @param url
	 * @param content
	 * @param params
	 * @return
	 * @throws TemplateModelException
	 */
	private String transformContent(String url, String content, Map<String, TemplateModel> params) throws TemplateModelException {
		String out = content;
		
		// Extract start-pattern
		TemplateModel param = params.get(Parameters.start.toString());
		if (param != null) {
			String regex = ((TemplateScalarModel) param).getAsString();
			out = Pattern.compile("^.*(" + regex + ")", Pattern.DOTALL | Pattern.MULTILINE).matcher(out).replaceAll("$1");
		}

		// Extract end-pattern
		param = params.get(Parameters.end.toString());
		if (param != null) {
			String regex = ((TemplateScalarModel) param).getAsString();
			out = Pattern.compile("(" + regex + ").*$", Pattern.DOTALL | Pattern.MULTILINE).matcher(out).replaceAll("$1");
		}
		
		// Convert relative URLs
		param = params.get(Parameters.convertrelative.toString());
		if (param != null) {
			boolean convert = ((TemplateBooleanModel) param).getAsBoolean();
			if (convert) {
				Matcher m = Pattern.compile("<([^>]*?)(href|src|action|background)\\s*=\\s*([\"|']?)(.*?)\\3((\\s+.*?)*)>", Pattern.DOTALL).matcher(out);
				
				int lastStart = 0;
				int lastEnd = 0;
				StringBuffer replaced = new StringBuffer();
				URI baseURI = URI.create(url);
				while(m.find()) {
					// Copy non-matched part
					replaced.append(out.substring(lastStart, m.start()));
					lastStart = m.end();
					lastEnd = m.end();
					
					replaced.append("<")
						.append(m.group(1)).append(m.group(2)).append("=\"")
						.append(baseURI.resolve(m.group(4)))
						.append("\"").append(m.group(5)).append(">");
				}

				// Copy last segment
				replaced.append(out.substring(lastEnd));
				out = replaced.toString();
			}
		}

		return out;
	}

}
