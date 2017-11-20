package com.funnelback.publicui.search.web.binding;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.LocaleResolver;

import com.funnelback.common.Environment.FunnelbackVersion;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.input.processors.PassThroughEnvironmentVariables;
import com.funnelback.publicui.search.model.transaction.ExecutionContext;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.service.log.LogUtils;
import com.funnelback.publicui.utils.MapKeyFilter;
import com.funnelback.publicui.utils.MapUtils;

import waffle.servlet.WindowsPrincipal;

public class SearchQuestionBinder {
    
    /**
     * Binds a {@link SearchQuestion} to another one by copying relevant fields.
     * 
     * We define 'relevant' here as things that might be bound initally from the request,
     * not things which are subsequently set by input processors.
     */
    public static void bind(SearchQuestion from, SearchQuestion to) {
        to.setExecutionContext(from.getExecutionContext());
        to.setFunnelbackVersion(from.getFunnelbackVersion());
        to.getRawInputParameters().putAll(from.getRawInputParameters());
        to.setQueryStringMap(from.getQueryStringMapCopy());
        to.setQuery(from.getQuery());
        to.setOriginalQuery(from.getOriginalQuery());
        to.setCollection(from.getCollection());
        to.setProfile(from.getProfile());
        to.setImpersonated(from.isImpersonated());
        to.setRequestId(from.getRequestId());
        to.setLocale(from.getLocale());
        to.setCnClickedCluster(from.getCnClickedCluster());
        to.getCnPreviousClusters().addAll(from.getCnPreviousClusters());
        to.setClive(from.getClive());
        to.getCustomData().putAll(from.getCustomData());
        to.setCurrentProfile(from.getCurrentProfile());
    }
    
    /**
     * Makes a clone of the given SearchQuestion
     * 
     * see {@link SearchQuestionBinder#bind(SearchQuestion, SearchQuestion)}
     * 
     * @param question
     * @return
     */
    public static SearchQuestion makeCloneOfReleventFields(SearchQuestion question) {
        SearchQuestion newQuestion = new SearchQuestion();
        bind(question, newQuestion);
        return newQuestion;
    }
    
    
    /**
     * Binds properties of the given {@link SearchQuestion} to the given {@link HttpServletRequest}
     * @param request
     * @param question
     */
    public static void bind(ExecutionContext executionContext,
            HttpServletRequest request,
            SearchQuestion question,
            LocaleResolver localeResolver,
            FunnelbackVersion funnelbackVersion) {
        question.getCollection().getProfiles();
        question.setExecutionContext(executionContext);
        question.setFunnelbackVersion(funnelbackVersion);
        question.getRawInputParameters().putAll(request.getParameterMap());
        
        // Add query string parameters, converting Map<String, String[]>
        // to Map<String, List<String>> for convenience
        Map<String, List<String>> queryStringMap = new HashMap<>();
        queryStringMap.putAll(
            request.getParameterMap()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                e -> e.getKey(),
                e -> Arrays.asList(e.getValue()))));
        question.setQueryStringMap(queryStringMap);
        
        // Add any HTTP servlet specifics
        String requestId = LogUtils.getRequestIdentifier(request,
            DefaultValues.RequestId.valueOf(question.getCollection()
                    .getConfiguration().value(Keys.REQUEST_ID_TO_LOG, DefaultValues.REQUEST_ID_TO_LOG.toString())),
                    question.getCollection().getConfiguration().value(Keys.Logging.IGNORED_X_FORWARDED_FOR_RANGES,
                            DefaultValues.Logging.IGNORED_X_FORWARDED_FOR_RANGES));

        //Environment variables for padre
        MapUtils.putAsStringArrayIfNotNull(
                question.getRawInputParameters(),
                PassThroughEnvironmentVariables.Keys.REMOTE_ADDR.toString(), requestId);
        MapUtils.putAsStringArrayIfNotNull(
                question.getRawInputParameters(),
                PassThroughEnvironmentVariables.Keys.REQUEST_URI.toString(), request.getRequestURI());
        MapUtils.putAsStringArrayIfNotNull(
                question.getRawInputParameters(),
                PassThroughEnvironmentVariables.Keys.AUTH_TYPE.toString(), request.getAuthType());
        MapUtils.putAsStringArrayIfNotNull(
                question.getRawInputParameters(),
                PassThroughEnvironmentVariables.Keys.HTTP_HOST.toString(), request.getHeader(SearchQuestion.RequestParameters.Header.HOST));
        MapUtils.putAsStringArrayIfNotNull(
                question.getRawInputParameters(),
                PassThroughEnvironmentVariables.Keys.REMOTE_USER.toString(), request.getRemoteUser());
        if (request.getRequestURL() != null) {
            question.getRawInputParameters()
                    .put(PassThroughEnvironmentVariables.Keys.REQUEST_URL.toString(),
                            new String[] { request.getRequestURL().toString()
                                    + ((request.getQueryString() != null) ? "?"    + request.getQueryString() : "") });
        }


        // Set locale
        question.setLocale(localeResolver.resolveLocale(request));
        
        // Copy original query
        question.setOriginalQuery(question.getQuery());
        
        // Is request impersonated ?
        question.setImpersonated(isRequestImpersonated(request));
        
        // User identifier to log
        question.setRequestId(requestId);
        
        // Last clicked cluster
        question.setCnClickedCluster(request.getParameter(RequestParameters.ContextualNavigation.CN_CLICKED));
        
        // Previously clicked clusters
        MapKeyFilter filter = new MapKeyFilter(request.getParameterMap());
        String[] paramNames = filter.filter(RequestParameters.ContextualNavigation.CN_PREV_PATTERN);
        Arrays.sort(paramNames);
        for(String paramName : paramNames) {
            // We don't really care of the indexes given in parameter names
            String value = request.getParameter(paramName);
            if (value != null && !"".equals(value) ) {
                question.getCnPreviousClusters().add(value);
            }
        }
        
        // Security Principal
        question.setPrincipal(request.getUserPrincipal());
        
        // Set currentProfile, based on profile, but ensuring it exists on disk for the given collection
        String currentProfile = question.getProfile();
        if (!question.getCollection().getProfiles().containsKey(currentProfile)) {
            currentProfile = DefaultValues.DEFAULT_PROFILE;
        }
        question.setCurrentProfile(currentProfile);
    }

    /**
     * <p>Detects if the request is impersonated.</p>
     * 
     * <p>TODO The current implementation doesn't really check if the user is impersonated
     * but only relies on the fact that it has been authenticated using the Waffle filter.
     * Unfortunately J2EE 5 doesn't allow us to have access to the FilterConfig to read
     * the value of the "impersonate" parameter (Possible with J2EE 6)</p>
     * <ul>
     *     <li>Either switch to J2EE 6</li>
     *     <li>Or update the WindowsPrincipal with a impersonation status field ?</li>
     * </ul>
     *   
     * @param request
     * @return true if the request is impersonated, false otherwise
     */
    private static boolean isRequestImpersonated(HttpServletRequest request) {
        return request.getUserPrincipal() != null && request.getUserPrincipal() instanceof WindowsPrincipal;
    }

}
