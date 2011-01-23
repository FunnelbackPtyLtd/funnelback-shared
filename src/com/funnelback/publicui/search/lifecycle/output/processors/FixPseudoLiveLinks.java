package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.apachecommons.Log;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.model.Collection.Type;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Fixes some "magic" URLs like "local://serve-...-document.cgi"
 * and transform them into really clickable links
 *
 */
@Component("fixPseudoLiveLinksOutputProcessor")
@Log
public class FixPseudoLiveLinks implements OutputProcessor {

	public static final Type[] SUPPORTED_TYPES = {
		Type.trim, Type.connector, Type.filecopy, Type.database
	};
	
	/** Local scheme as used in DB or Connector collection */
	private static final String LOCAL_SCHEME = "local://";
	
	/** Prefix to use for FileCopy links */
	private static final String FILECOPY_PREFIX = "serve-filecopy-document.cgi";
	
	/** Prefix to use for TRIM links */
	private static final String TRIM_PREFIX = "serve-trim-";
	
	/** Pattern to parse TRIM pseudo live URLs */
	private static final Pattern TRIM_URL_PATTERN = Pattern.compile("trim://../(\\d+)/.*$");
	
	/** Pattern to extract the URL of the cached version from the cache url */
	private static final Pattern TRIM_CACHE_URL_PATTERN = Pattern.compile(".*doc=([^&]*).*");
	
	@Override
	public void process(SearchTransaction searchTransaction) {
		// Only process some specific collection types
		if (ArrayUtils.contains(SUPPORTED_TYPES, searchTransaction.getQuestion().getCollection().getType())) {
			// Ensure we have something to do
			if (searchTransaction.hasResponse()
				&& searchTransaction.getResponse().hasResultPacket()
				&& searchTransaction.getResponse().getResultPacket().hasResults()) {
			
				String trimDefaultLiveLinks = searchTransaction.getQuestion().getCollection().getConfiguration().value(Keys.Trim.DEFAULT_LIVE_LINKS);
				
				// 	Iterate over the results
				for (Result result : searchTransaction.getResponse().getResultPacket().getResults()) {
					String transformedLiveUrl = result.getLiveUrl();
					switch (searchTransaction.getQuestion().getCollection().getType()) {
					
					case database:
					case connector:
						// Simply strip off "local://"
						if (result.getLiveUrl().startsWith(LOCAL_SCHEME)) {
							// TODO use a proper regexp ?
							transformedLiveUrl = result.getLiveUrl().substring(LOCAL_SCHEME.length());
						}
						break;
					
					case trim:
						// Replace pseudo URLs trim://45/312 with serve-trim-(document|reference).cgi
						Matcher liveUrlMatcher = TRIM_URL_PATTERN.matcher(result.getLiveUrl());
						Matcher cachedUrlMatcher = TRIM_CACHE_URL_PATTERN.matcher(result.getCacheUrl());
						
						if (liveUrlMatcher.find() && cachedUrlMatcher.find()) {
							String docUri = liveUrlMatcher.group(1);
							String cachedUri = cachedUrlMatcher.group(1);
							transformedLiveUrl = TRIM_PREFIX + trimDefaultLiveLinks
								+ ".cgi?"+RequestParameters.COLLECTION+"=" + searchTransaction.getQuestion().getCollection().getId()
								+ "&"+RequestParameters.Serve.URI+"=" + docUri
								+ "&"+RequestParameters.Serve.DOC+"=" + cachedUri;
						
						} else {
							log.warn("Unable to parse TRIM URLs (live='"
									+ result.getLiveUrl() + "', cached='"
									+ result.getCacheUrl()+ "'. URL won't be transformed.");
						}
						break;
					
					case filecopy:
						// Prefix with serve-filecopy-document.cgi
						transformedLiveUrl = FILECOPY_PREFIX
							+ "?"+RequestParameters.COLLECTION+"="+searchTransaction.getQuestion().getCollection().getId()
							+ "&"+RequestParameters.Serve.URI+"="+result.getLiveUrl();
						
						break;
					}
					
					log.debug("Live URL transformed from '"+result.getLiveUrl()+"' to '"+transformedLiveUrl+"'");
					result.setLiveUrl(transformedLiveUrl);
				}
			
			}
		}

	}

}
