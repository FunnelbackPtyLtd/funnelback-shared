package com.funnelback.publicui.search.lifecycle.output.processors;

import static com.funnelback.config.keys.Keys.ServerKeys;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Collection.Type;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.padre.utils.ResultUtils;
import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.auth.AuthTokenManager;
import com.google.common.collect.ImmutableSet;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

/**
 * Fixes some "magic" URLs like "local://serve-...-document.cgi"
 * and transform them into really clickable links
 *
 */
@Component("fixPseudoLiveLinksOutputProcessor")
@Log4j2
public class FixPseudoLiveLinks extends AbstractOutputProcessor {

    @Autowired
    private ConfigRepository configRepository;
    
    @Autowired
    private AuthTokenManager authTokenManager;
    
    @Value("#{appProperties['urls.search.prefix']}")
    private String searchUrlPrefix;
    
    /** Collection type supported by this input processor */
    public static final Set<Type> SUPPORTED_TYPES = ImmutableSet.of(
        Type.connector, Type.trim, Type.filecopy, Type.database, Type.directory, Type.push, Type.push2);
    
    
    /** CGI suffix for Classic UI links */
    private static final String CGI_SUFFIX = ".cgi";
    
    /** Pattern to parse TRIM pseudo live URLs */
    private static final Pattern TRIM_URL_PATTERN = Pattern.compile("trim://../(\\d+)/?.*$");
    
    /** Pattern to extract the URL of the cached version from the cache url */
    private static final Pattern TRIM_CACHE_URL_PATTERN = Pattern.compile(".*doc=([^&]*).*");
    
    @Override
    public void processOutput(SearchTransaction searchTransaction) {
        // Ensure we have something to do
        if (SearchTransactionUtils.hasCollection(searchTransaction)
            && SearchTransactionUtils.hasResults(searchTransaction)) {
            
            //     Iterate over the results
            for (Result result : searchTransaction.getResponse().getResultPacket().getResults()) {
                
                
                // Most of the time the result will be coming from the same collection as
                // the question, except for meta collections
                Collection resultCollection = searchTransaction.getQuestion().getCollection();
                if (! searchTransaction.getQuestion().getCollection().getId().equals(result.getCollection())) {
                    resultCollection = configRepository.getCollection(result.getCollection());
                }
                
                if ( resultCollection == null) {
                    log.warn("Invalid collection '" + result.getCollection() + "' for result '" + result + "'");
                    continue;
                }
                
                // Only process some specific collection types
                if (! SUPPORTED_TYPES.contains(resultCollection.getType())) {
                    continue;
                }
                
                String transformedLiveUrl = getTransformedUrl(
                    resultCollection,
                    getResultCollectionType(resultCollection, result),
                    result);
                
                log.debug("Live URL transformed from '"+result.getLiveUrl()+"' to '"+transformedLiveUrl+"'");
                result.setLiveUrl(transformedLiveUrl);
            }
        
        }
    }
    
    /**
     * <p>Get the type of a result, depending on its collection</p>
     * 
     * <p>For all collections the type is the collection type, except for
     * {@link Type#push} and {@link Type#push2} collection that can contains
     * various result types. In that case the type is deduced from the URL</p>
     * 
     * @param resultCollection
     * @param r
     * @return
     */
    private Type getResultCollectionType(Collection resultCollection, Result r) {
        Type type = resultCollection.getType();
        if (Type.push.equals(type)
            || Type.push2.equals(type)) {
            return ResultUtils.getCollectionTypeFromURL(r.getLiveUrl());
        } else {
            return type;
        }
    }
    
    /**
     * Get the transformed URL for a result, depending on its type
     * @param resultCollection Collection the result is coming from
     * @param resultCollectionType Type of the result. Identical to the collection type
     * except for Push collections where it will be different as the result has been
     * pushed from a different collection type (trimpush, filecopy, etc.)
     * @param result Result with the URL to transform
     * @return The transformed URL
     */
    @SneakyThrows(UnsupportedEncodingException.class)
    private String getTransformedUrl(Collection resultCollection, Type resultCollectionType, Result result) {
        String transformedLiveUrl = result.getLiveUrl();
        
        switch (resultCollectionType) {
        
        case database:
            if((result.getLiveUrl().startsWith(ResultUtils.DATABASE_URL)
                || result.getLiveUrl().startsWith(ResultUtils.OLD_DATABASE_URL)) && result.getCacheUrl() != null) {
                return result.getCacheUrl();
            }
            break;
        
        case directory:
            if((result.getLiveUrl().startsWith(ResultUtils.DIRECTORY_URL)
                || result.getLiveUrl().startsWith(ResultUtils.OLD_DIRECTORY_URL)) && result.getCacheUrl() != null) {
                return result.getCacheUrl();
            }
            break;
        case connector:
            if((result.getLiveUrl().startsWith(ResultUtils.CONNECTOR_URL)
                || result.getLiveUrl().startsWith(ResultUtils.OLD_CONNECTOR_URL)) && result.getCacheUrl() != null) {
                return result.getCacheUrl();
            }
            break;
        case trim:
        case trimpush:
            // Replace pseudo URLs trim://45/312 with serve-trim-(document|reference).cgi
            Matcher liveUrlMatcher = TRIM_URL_PATTERN.matcher(result.getLiveUrl());
            Matcher cachedUrlMatcher = TRIM_CACHE_URL_PATTERN.matcher(result.getCacheUrl());
            
            if (liveUrlMatcher.find()) {
                String docUri = liveUrlMatcher.group(1);
                
                // 'reference' or 'document'
                String trimDefaultLiveLinks = resultCollection
                    .getConfiguration().value(Keys.Trim.DEFAULT_LIVE_LINKS);
                
                // Lookup collection.cfg for 'ui.modern.serve.trim.(document|reference)_link'
                String trimLinkPrefix = resultCollection.getConfiguration()
                    .value(Keys.ModernUI.Serve.TRIM_LINK_PREFIX,
                        DefaultValues.ModernUI.Serve.TRIM_CLASSIC_LINK_PREFIX);
                
                String trimLinkSuffix = "";
                if (DefaultValues.ModernUI.Serve.TRIM_CLASSIC_LINK_PREFIX.equals(trimLinkPrefix)) {
                    // Classic UI, prefix with /search/, suffix with .cgi
                    trimLinkPrefix = searchUrlPrefix + trimLinkPrefix;
                    trimLinkSuffix = CGI_SUFFIX;
                }
                
                
                transformedLiveUrl = trimLinkPrefix + trimDefaultLiveLinks + trimLinkSuffix
                    + "?"+RequestParameters.COLLECTION + "=" + resultCollection.getId()
                    + "&"+RequestParameters.Serve.URI + "=" + URLEncoder.encode(docUri, "UTF-8")
                    + "&"+RequestParameters.Cache.URL + "=" + URLEncoder.encode(result.getLiveUrl(), "UTF-8");
                    
                if (cachedUrlMatcher.find()) {
                    transformedLiveUrl += "&"+RequestParameters.Serve.DOC+"=" + URLEncoder.encode(cachedUrlMatcher.group(1), "UTF-8");
                }
            
            } else {
                log.warn("Unable to parse TRIM URLs (live='"
                        + result.getLiveUrl() + "', cached='"
                        + result.getCacheUrl()+ "'. URL won't be transformed.");
            }
            break;
        
        case filecopy:
            // FUN-5472 Add a token to prevent people fiddling with the file URI
            String securityToken = authTokenManager.getToken(result.getLiveUrl(),
                configRepository.getServerConfig().get(ServerKeys.SERVER_SECRET));

            // Prefix with serve-filecopy-document.cgi
            transformedLiveUrl =  resultCollection
                .getConfiguration().value(Keys.ModernUI.Serve.FILECOPY_LINK,
                    searchUrlPrefix + DefaultValues.ModernUI.Serve.FILECOPY_LINK)
                + "?"+RequestParameters.COLLECTION+"="+resultCollection.getId()
                + "&"+RequestParameters.Serve.URI+"="+URLEncoder.encode(result.getLiveUrl(), "UTF-8")
                + "&"+RequestParameters.Serve.AUTH+"="+URLEncoder.encode(securityToken, "UTF-8");
            
            break;
        default:
            // Do nothing
        }

        return transformedLiveUrl;
    }

}
