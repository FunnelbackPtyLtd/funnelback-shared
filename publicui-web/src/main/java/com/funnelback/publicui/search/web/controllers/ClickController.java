package com.funnelback.publicui.search.web.controllers;

import static com.funnelback.publicui.utils.web.MetricsConfiguration.ALL_NS;
import static com.funnelback.publicui.utils.web.MetricsConfiguration.VIEW_TYPE_NS;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

import com.codahale.metrics.MetricRegistry;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.log.ClickLog;
import com.funnelback.publicui.search.model.log.InteractionLog;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.session.ClickHistory;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.IndexRepository;
import com.funnelback.publicui.search.service.SearchHistoryRepository;
import com.funnelback.publicui.search.service.auth.AuthTokenManager;
import com.funnelback.publicui.search.service.log.LogService;
import com.funnelback.publicui.search.service.log.LogUtils;
import com.funnelback.publicui.search.web.controllers.session.SessionController;
import com.funnelback.publicui.utils.QueryStringUtils;
import com.funnelback.publicui.utils.web.MetricsConfiguration;

/**
 * Click tracking controller
 */
@Log4j
@Controller
public class ClickController extends SessionController {

    /** Parameters not to include in the payload part of interaction logs */
    private static final String[] BORING_INTERACTION_PARAMETERS = new String[] {
        RequestParameters.COLLECTION,
        RequestParameters.Click.TYPE
    };

    @Autowired
    private LogService logService;
    
    /** HTTP Referer header */
    private static final String REFERER = "referer"; 
   
    @Autowired
    private AuthTokenManager authTokenManager;

    @Autowired
    private ConfigRepository configRepository;
    
    @Autowired
    private IndexRepository indexRepository;
    
    @Autowired
    private LocaleResolver localeResolver;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Autowired
    private MetricRegistry metrics;

    /**
     * Controller for interaction logging. 
     * 
     * @param request HTTP request 
     * @param response HTTP response
     * @param collectionId Collection to log for
     * @param profile Profile to log for
     * @param logType type of interaction
     * @param user Current user from the session
     */
    @RequestMapping(value = "/log", method = RequestMethod.GET)
    public void log(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(required= true, value = RequestParameters.COLLECTION) String collectionId,
            @RequestParam(required = false) String profile,
            @RequestParam(required = true, value = RequestParameters.Click.TYPE) String logType,
            @ModelAttribute SearchUser user) {
    
        Collection collection = configRepository.getCollection(collectionId);
        
        if (collection != null) {
            // Get the user id
            String requestId = LogUtils.getRequestIdentifier(request,
                    DefaultValues.RequestId.valueOf(collection.getConfiguration()
                    		.value(Keys.REQUEST_ID_TO_LOG, 
                    				DefaultValues.REQUEST_ID_TO_LOG.toString())),
                    collection.getConfiguration()
                    		.value(Keys.Logging.IGNORED_X_FORWARDED_FOR_RANGES,
                    				DefaultValues.Logging.IGNORED_X_FORWARDED_FOR_RANGES));

            URL referer = getReferrer(request);
            
            Set<String> boringParameters =  new HashSet<String>(Arrays.asList(BORING_INTERACTION_PARAMETERS));
            
            Map<String, String[]> parameters = new HashMap<String, String[]>(request.getParameterMap());
            parameters.keySet().removeAll(boringParameters);
            
            logService.logInteraction(
                new InteractionLog(new Date(), collection, collection.getProfiles().get(profile),
                    requestId, logType, referer, parameters,
                    LogUtils.getUserId(user)));
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    
    /**
     * Log the click and redirect the user to the document URL
     * 
     * @param request HTTP Request
     * @param response HTTP response
     * @param collectionId ID of the collection
     * @param type Faceted nav, Cluster, result click, etc.
     * @param rank Rank of the clicked result
     * @param profile Current profile
     * @param redirectUrl URL to redirect to
     * @param authtoken Token to check that the link was built by Funnelback
     * @param noAttachment Special parameter to stream the content directly to the browser,
     * used in automated testing
     * @param user User from the search session
     * @throws IOException If something goes wrong
     */
    @RequestMapping(value = "/redirect", method = RequestMethod.GET)
    public void redirect(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(RequestParameters.COLLECTION) String collectionId,
            @RequestParam(required = false, defaultValue = "CLICK") ClickLog.Type type,
            @RequestParam(required = false, defaultValue = "0") Integer rank,
            @RequestParam(required = false, defaultValue = DefaultValues.DEFAULT_PROFILE) String profile,
            @RequestParam(value = RequestParameters.Click.URL, required = true) URI redirectUrl,
            @RequestParam(value = RequestParameters.Click.AUTH, required = true) String authtoken,
            @RequestParam(value = RequestParameters.Click.NOATTACHMENT,
                           required = false, defaultValue = "false") boolean noAttachment,
            @ModelAttribute SearchUser user) throws IOException {

        Collection collection = configRepository.getCollection(collectionId);
        
        if (collection != null) {
            // Does the token match the target? Forbidden if not.
            if (!authTokenManager.checkToken(authtoken, redirectUrl.toString(),
                    collection.getConfiguration().value(Keys.SERVER_SECRET))) {
                if (log.isDebugEnabled()) {
                    log.debug("Invalid token for URL '"+redirectUrl.toString()+"', expected '"
                        + authTokenManager.getToken(redirectUrl.toString(), collection.getConfiguration()
                            .value(Keys.SERVER_SECRET))
                        + "' but got '" + authtoken + "'");
                }
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // Get the user id
            String requestId = LogUtils.getRequestIdentifier(request,
                    DefaultValues.RequestId.valueOf(collection.getConfiguration()
                    		.value(Keys.REQUEST_ID_TO_LOG, 
                    				DefaultValues.REQUEST_ID_TO_LOG.toString())),
                    collection.getConfiguration()
                    		.value(Keys.Logging.IGNORED_X_FORWARDED_FOR_RANGES,
                    				DefaultValues.Logging.IGNORED_X_FORWARDED_FOR_RANGES));
            
            URL referer = getReferrer(request);
            
            if (collection.getConfiguration().valueAsBoolean(Keys.ModernUI.SESSION, DefaultValues.ModernUI.SESSION)
                && user != null) {
                // Save the click in the user history
                Result r = indexRepository.getResult(collection, redirectUrl);
                if (r != null) {
                
                    ClickHistory h = ClickHistory.fromResult(r);
                    h.setCollection(collection.getId());
                    h.setClickDate(new Date());
                    h.setUserId(user.getId());
                    if (referer != null) {
                        Map<String, String> qs = QueryStringUtils.toSingleMap(referer.getQuery());
                        if (qs != null && qs.containsKey(RequestParameters.QUERY)) {
                            h.setQuery(qs.get(RequestParameters.QUERY));
                        }
                    }
                    
                    try {
                        searchHistoryRepository.saveClick(h);
                    } catch (DataAccessException | TransactionException e) {
                        log.error("Error while saving click history", e);
                    }
                } else {
                    log.warn("Result with URL '"+redirectUrl+"' not found in collection '"+collection.getId()+"'");
                }
            }
            
            logService.logClick(new ClickLog(new Date(), collection, collection
                    .getProfiles().get(profile), requestId, referer, rank,
                    redirectUrl, type, LogUtils.getUserId(user)));
            
            metrics.counter(MetricRegistry.name(
                MetricsConfiguration.ALL_NS, MetricsConfiguration.CLICK)).inc();

            metrics.counter(MetricRegistry.name(
                MetricsConfiguration.COLLECTION_NS, collection.getId(),
                profile, MetricsConfiguration.CLICK)).inc();
            
            response.sendRedirect(
                redirectUrl.toString()
                + ( noAttachment
                    ? "&"+RequestParameters.Click.NOATTACHMENT+"="+Boolean.toString(noAttachment)
                    : "")
            );
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Helper method to get the HTTP referrer out of a request
     * 
     * @param request 
     * @return the URL for the HTTP referrer.
     */
    private URL getReferrer(HttpServletRequest request) {
        URL referer = null;
        if (request.getHeader(REFERER) != null) {
            try {
                referer = new URL(request.getHeader(REFERER));
            } catch (MalformedURLException mue) {
                log.warn(
                        "Unable to parse referer '"
                                + request.getHeader(REFERER) + "'", mue);
            }
        }
        return referer;
    }

}
