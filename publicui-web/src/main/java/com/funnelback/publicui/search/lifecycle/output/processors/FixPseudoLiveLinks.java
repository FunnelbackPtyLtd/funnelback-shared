package com.funnelback.publicui.search.lifecycle.output.processors;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.SneakyThrows;
import lombok.extern.apachecommons.Log;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Collection.Type;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.service.ConfigRepository;

/**
 * Fixes some "magic" URLs like "local://serve-...-document.cgi"
 * and transform them into really clickable links
 *
 */
@Component("fixPseudoLiveLinksOutputProcessor")
@Log
public class FixPseudoLiveLinks implements OutputProcessor {

	@Autowired
	private ConfigRepository configRepository;
	
	@Value("#{appProperties['urls.search.prefix']}")
	private String searchUrlPrefix;
	
	public static final Type[] SUPPORTED_TYPES = {
		Type.trim, Type.connector, Type.filecopy, Type.database, Type.directory
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
	@SneakyThrows(UnsupportedEncodingException.class)
	public void processOutput(SearchTransaction searchTransaction) {
		// Ensure we have something to do
		if (SearchTransactionUtils.hasResults(searchTransaction)) {
			
			// 	Iterate over the results
			for (Result result : searchTransaction.getResponse().getResultPacket().getResults()) {
				String transformedLiveUrl = result.getLiveUrl();
				Collection resultCollection = configRepository.getCollection(result.getCollection());
				if ( resultCollection == null) {
					log.warn("Invalid collection '" + result.getCollection() + "' for result '" + result + "'");
					continue;
				}
				
				// Only process some specific collection types
				if (! ArrayUtils.contains(SUPPORTED_TYPES, resultCollection.getType())) {
					continue;
				}
				
				switch (resultCollection.getType()) {
				
				case database:
				case connector:
				case directory:
					// Simply strip off "local://"
					if (result.getLiveUrl().startsWith(LOCAL_SCHEME)) {
						// TODO use a proper regexp ?
						transformedLiveUrl = searchUrlPrefix + result.getLiveUrl().substring(LOCAL_SCHEME.length());
					}
					break;
				
				case trim:
					// Replace pseudo URLs trim://45/312 with serve-trim-(document|reference).cgi
					Matcher liveUrlMatcher = TRIM_URL_PATTERN.matcher(result.getLiveUrl());
					Matcher cachedUrlMatcher = TRIM_CACHE_URL_PATTERN.matcher(result.getCacheUrl());
					
					if (liveUrlMatcher.find()) {
						String trimDefaultLiveLinks = resultCollection.getConfiguration().value(Keys.Trim.DEFAULT_LIVE_LINKS);
						
						String docUri = liveUrlMatcher.group(1);
						
						transformedLiveUrl = searchUrlPrefix + TRIM_PREFIX + trimDefaultLiveLinks
							+ ".cgi?"+RequestParameters.COLLECTION+"=" + resultCollection.getId()
							+ "&"+RequestParameters.Serve.URI+"=" + docUri;
							
						if (cachedUrlMatcher.find()) {
							transformedLiveUrl += "&"+RequestParameters.Serve.DOC+"=" + cachedUrlMatcher.group(1);
						}
					
					} else {
						log.warn("Unable to parse TRIM URLs (live='"
								+ result.getLiveUrl() + "', cached='"
								+ result.getCacheUrl()+ "'. URL won't be transformed.");
					}
					break;
				
				case filecopy:
					// Prefix with serve-filecopy-document.cgi
					transformedLiveUrl = searchUrlPrefix + FILECOPY_PREFIX
						+ "?"+RequestParameters.COLLECTION+"="+resultCollection.getId()
						+ "&"+RequestParameters.Serve.URI+"="+URLEncoder.encode(result.getLiveUrl(), "UTF-8");
					
					break;
				}
				
				log.debug("Live URL transformed from '"+result.getLiveUrl()+"' to '"+transformedLiveUrl+"'");
				result.setLiveUrl(transformedLiveUrl);
			}
		
		}

	}

}
