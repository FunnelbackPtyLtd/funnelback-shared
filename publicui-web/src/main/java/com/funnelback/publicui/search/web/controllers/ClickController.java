package com.funnelback.publicui.search.web.controllers;

import java.io.IOException;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.codahale.metrics.MetricRegistry;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.common.config.ProfileId;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.log.ClickLog;
import com.funnelback.publicui.search.model.log.InteractionLog;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.session.ClickHistory;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.ConfigRepository;
import com.funnelback.publicui.search.service.IndexRepository;
import com.funnelback.publicui.search.service.SearchHistoryRepository;
import com.funnelback.publicui.search.service.auth.AuthTokenManager;
import com.funnelback.publicui.search.service.log.LogService;
import com.funnelback.publicui.search.service.log.LogUtils;
import com.funnelback.publicui.search.web.binding.CollectionEditor;
import com.funnelback.publicui.search.web.binding.ProfileEditor;
import com.funnelback.publicui.search.web.controllers.session.SessionController;
import com.funnelback.publicui.utils.QueryStringUtils;
import com.funnelback.publicui.utils.web.MetricsConfiguration;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * Click tracking controller
 */
@Log4j2
@Controller
public class ClickController extends SessionController {

    /** Parameters not to include in the payload part of interaction logs */
    private static final String[] BORING_INTERACTION_PARAMETERS = new String[] {
        RequestParameters.COLLECTION,
        RequestParameters.Click.TYPE
    };

    @Autowired
    private LogService logService;
   
    @Autowired
    @Setter private AuthTokenManager authTokenManager;

    @Autowired
    private ConfigRepository configRepository;
    
    @Autowired
    private IndexRepository indexRepository;
    
    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Autowired
    private MetricRegistry metrics;

    @InitBinder
    public void initBinder(DataBinder binder) {
        binder.registerCustomEditor(Collection.class, new CollectionEditor(configRepository));
        binder.registerCustomEditor(ProfileId.class, new ProfileEditor(DefaultValues.DEFAULT_PROFILE));
    }
    
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
            @RequestParam(required= true, value = RequestParameters.COLLECTION) Collection collection,
            @RequestParam(required = false, defaultValue = DefaultValues.DEFAULT_PROFILE) ProfileId profile,
            @RequestParam(required = true, value = RequestParameters.Click.TYPE) String logType,
            @RequestParam(required = false) String callback,
            @ModelAttribute SearchUser user) {
    
        if (collection != null) {
            // Get the user id
            String requestId = LogUtils.getRequestIdentifier(request,
                    DefaultValues.RequestId.valueOf(collection.getConfiguration()
                            .value(Keys.REQUEST_ID_TO_LOG, 
                                    DefaultValues.REQUEST_ID_TO_LOG.toString())),
                    collection.getConfiguration()
                            .value(Keys.Logging.IGNORED_X_FORWARDED_FOR_RANGES,
                                    DefaultValues.Logging.IGNORED_X_FORWARDED_FOR_RANGES));

            URL referer = LogUtils.getReferrer(request);
            
            Set<String> boringParameters =  new HashSet<String>(Arrays.asList(BORING_INTERACTION_PARAMETERS));
            
            Map<String, String[]> parameters = new HashMap<String, String[]>(request.getParameterMap());
            parameters.keySet().removeAll(boringParameters);
            
            logService.logInteraction(
                new InteractionLog(new Date(), collection, collection.getProfiles().get(profile.getId()),
                    requestId, logType, referer, parameters,
                    LogUtils.getUserId(user)));
            
            response.setStatus(HttpServletResponse.SC_OK);
            if (callback != null) {
                try {
                    response.getWriter().append(callback + "()");
                } catch (IOException e) {
                    throw new RuntimeException("Exception writing jsonp callback output.", e);
                }
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
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
            @RequestParam(RequestParameters.COLLECTION) Collection collection,
            @RequestParam(required = false, defaultValue = "CLICK") ClickLog.Type type,
            @RequestParam(required = false, defaultValue = "0") Integer rank,
            @RequestParam(required = false, defaultValue = DefaultValues.DEFAULT_PROFILE) ProfileId profile,
            @RequestParam(value = RequestParameters.Click.URL, required = true) URI redirectUrl,
            @RequestParam(value = RequestParameters.Click.INDEX_URL, required = true) URI indexUrl,
            @RequestParam(value = RequestParameters.Click.AUTH, required = true) String authtoken,
            @RequestParam(value = RequestParameters.Click.NOATTACHMENT,
                           required = false, defaultValue = "false") boolean noAttachment,
            @ModelAttribute SearchUser user) throws IOException {

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
            
            try {    
                // Get the user id
                String requestId = LogUtils.getRequestIdentifier(request,
                        DefaultValues.RequestId.valueOf(collection.getConfiguration()
                                .value(Keys.REQUEST_ID_TO_LOG, 
                                        DefaultValues.REQUEST_ID_TO_LOG.toString())),
                        collection.getConfiguration()
                                .value(Keys.Logging.IGNORED_X_FORWARDED_FOR_RANGES,
                                        DefaultValues.Logging.IGNORED_X_FORWARDED_FOR_RANGES));
                
                URL referer = LogUtils.getReferrer(request);
                
                if (collection.getConfiguration().valueAsBoolean(Keys.ModernUI.SESSION, DefaultValues.ModernUI.SESSION)
                    && user != null) {
                    // Save the click in the user history
                    Result r = indexRepository.getResult(collection, indexUrl);
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
                        
                        searchHistoryRepository.saveClick(h);
                    } else {
                        log.warn("Result with URL '"+indexUrl+"' not found in collection '"+collection.getId()+"'");
                    }
                }
                
                logService.logClick(new ClickLog(new Date(), collection, collection
                        .getProfiles().get(profile.getId()), requestId, referer, rank,
                        indexUrl, type, LogUtils.getUserId(user)));
                
                metrics.counter(MetricRegistry.name(
                    MetricsConfiguration.ALL_NS, MetricsConfiguration.CLICK)).inc();
    
                metrics.counter(MetricRegistry.name(
                    MetricsConfiguration.COLLECTION_NS, collection.getId(),
                    profile.getId(), MetricsConfiguration.CLICK)).inc();
            } catch (Exception e) {
                log.error("Error while processing click", e);
            }
            
            // Always try to redirect, even if there was an Exception before (SUPPORT-1982)
            response.sendRedirect(
                redirectUrl.toString()
                + ( noAttachment
                    ? "&"+RequestParameters.Click.NOATTACHMENT+"="+Boolean.toString(noAttachment)
                    : "")
            );
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

}
