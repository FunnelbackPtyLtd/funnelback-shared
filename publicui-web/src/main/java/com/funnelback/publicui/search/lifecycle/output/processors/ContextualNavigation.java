package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.padre.Category;
import com.funnelback.publicui.search.model.padre.Cluster;
import com.funnelback.publicui.search.model.padre.ClusterNav;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.utils.QueryStringUtils;
import com.google.common.collect.Sets;

/**
 * Process PADRE contextual navigation response:
 * - Cleans up links from padre (removing outdated paths, irrelevant parameters)
 * - Adds tacking information for logging purposes
 * 
 *
 */
@Component("contextualNavigationOutputProcessor")
public class ContextualNavigation extends AbstractOutputProcessor {
    
    @Override
    public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
        if (SearchTransactionUtils.hasResponse(searchTransaction)
                && searchTransaction.getResponse().hasResultPacket()
                && searchTransaction.getResponse().getResultPacket().getContextualNavigation() != null) {
            
            ClusterNav clusterNav = searchTransaction.getResponse().getResultPacket().getContextualNavigation().getClusterNav();
            clusterNav.setUrl(cleanContextualNavigationLink(searchTransaction.getQuestion().getQueryStringMapCopy(), clusterNav.getUrl()));
            
            for (Category category: searchTransaction.getResponse().getResultPacket().getContextualNavigation().getCategories()) {
                if (category.getMoreLink() != null) {
                    category.setMoreLink(cleanContextualNavigationLink(searchTransaction.getQuestion().getQueryStringMapCopy(), category.getMoreLink()));
                }
                if (category.getFewerLink() != null) {
                    category.setFewerLink(cleanContextualNavigationLink(searchTransaction.getQuestion().getQueryStringMapCopy(), category.getFewerLink()));
                }
                
                // Sets additional parameters to the URL for logging purposes
                for (Cluster cluster: category.getClusters()) {
                    cluster.setHref(cleanContextualNavigationLink(searchTransaction.getQuestion().getQueryStringMapCopy(), cluster.getHref()));
                    
                    Map<String, List<String>> qs = QueryStringUtils.toMap(cluster.getHref());

                    // Remove repeated parameters from PADRE
                    for(String key : qs.keySet().toArray(new String[0])) {
                        if (RequestParameters.ContextualNavigation.CN_CLICKED.equals(key)
                            || key.startsWith(RequestParameters.ContextualNavigation.CN_PREV_PREFIX)) {
                            qs.remove(key);
                        }
                    }
                    
                    if (cluster.getQuery() != null) {
                        qs.put(
                                RequestParameters.ContextualNavigation.CN_CLICKED,
                                Arrays.asList(new String[] {cluster.getQuery().toLowerCase()}));
                    }
                    
                    if (searchTransaction.getQuestion().getCnPreviousClusters().size() < 1) {
                        // Append initial query
                        qs.put(
                                RequestParameters.ContextualNavigation.CN_PREV_PREFIX+"0",
                                Arrays.asList(new String[] {searchTransaction.getQuestion().getQuery()}));
                    } else {
                        int i=0;
                        for (; i< searchTransaction.getQuestion().getCnPreviousClusters().size(); i++) {
                            qs.put(
                                    RequestParameters.ContextualNavigation.CN_PREV_PREFIX+i,
                                    Arrays.asList(new String[] {searchTransaction.getQuestion().getCnPreviousClusters().get(i)}));
                        }
                        qs.put(
                                RequestParameters.ContextualNavigation.CN_PREV_PREFIX+i,
                                Arrays.asList(new String[] {searchTransaction.getQuestion().getCnClickedCluster()}));
                    }
                    cluster.setHref(QueryStringUtils.toString(qs, true));
                }
            }
        }

    }
    
    private final static Set<String> PADRE_CONTEXTUAL_NAVIGATION_QUERY_PARAMETERS_TO_KEEP = Sets.newHashSet("site_max_clusters", "topic_max_clusters", "type_max_clusters", "query");
    
    private final static Set<String> USER_QUERY_PARAMETERS_TO_CLEAR = Sets.newHashSet("start_rank");
    
    /**
     * Cleans up a padre-generated contextual navigation link for modern-ui usage. In particular it will...
     * 
     * - Remove anything before the first '?' if one is present (because padre may start links 
     *   with /search/padre-sw.cgi?...). If no '?' is present, the return value will not have one either.
     *   
     * - Keep only parameters that are set in the original userQueryStringMapCopy or that are specifically set by padre
     *   (see PADRE_CONTEXTUAL_NAVIGATION_QUERY_PARAMTERS_TO_KEEP). This avoids double-applying a gscope which a facet
     *   causes to be set, but which is passed to padre via the query_string.
     *   
     *   Some userQueryStringMapCopy parameters will be removed since they may not make sense after the contextual 
     *   navigation link is clicked. For example 'start_rank'. See USER_QUERY_PARAMETERS_TO_CLEAR.
     */
    public /* private if not for unit tests */ static String cleanContextualNavigationLink(Map<String, List<String>> userQueryStringMapCopy, String padreGeneratedLink) {
        Map<String, List<String>> contextualNavigationLinkQueryStringMap;
        Boolean prependQuestionMark;
        if (padreGeneratedLink.contains("?")) {
            contextualNavigationLinkQueryStringMap = QueryStringUtils.toMap(padreGeneratedLink.substring(padreGeneratedLink.indexOf("?") + 1));
            prependQuestionMark = true;
        } else {
            contextualNavigationLinkQueryStringMap = QueryStringUtils.toMap(padreGeneratedLink);
            prependQuestionMark = false;            
        }

        Map<String, List<String>> resultMap = new HashMap<>();
        userQueryStringMapCopy.entrySet().stream().filter(entry -> {
            return !USER_QUERY_PARAMETERS_TO_CLEAR.contains(entry.getKey());
        }).forEach(entry -> {
            resultMap.put(entry.getKey(), entry.getValue());
        });
        

        for (String param : PADRE_CONTEXTUAL_NAVIGATION_QUERY_PARAMETERS_TO_KEEP) {
            if (contextualNavigationLinkQueryStringMap.containsKey(param)) {
                resultMap.put(param, contextualNavigationLinkQueryStringMap.get(param));
            }
        }
        

        return (QueryStringUtils.toString(resultMap, prependQuestionMark));
    }

}
