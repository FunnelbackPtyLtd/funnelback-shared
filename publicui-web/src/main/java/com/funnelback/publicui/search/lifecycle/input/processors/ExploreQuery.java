package com.funnelback.publicui.search.lifecycle.input.processors;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.curator.action.RemoveUrls;
import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.explore.ExploreQueryGenerator;
import com.funnelback.publicui.search.model.curator.config.Action;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.utils.MapUtils;

/**
 * Processes explore:... queries. Calls padre-rf to
 * get query terms and replace the explore:... query with those.
 */
@Component("exploreQueryInputProcessor")
@Log4j
public class ExploreQuery extends AbstractInputProcessor {

    private static final String OPT_VSIMPLE = "-vsimple=on";
    
    private final String EXPLORE_PREFIX = "explore:";
    
    @Autowired
    @Setter private ExploreQueryGenerator generator;
    
    @Autowired
    private ApplicationContext applicat;
    
    @Override
    public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
        if (SearchTransactionUtils.hasQueryAndCollection(searchTransaction)) {
            
            Integer nbOfTerms = null;
            if (searchTransaction.getQuestion().getRawInputParameters().get(RequestParameters.EXP) != null) {
                String exp = MapUtils.getFirstString(searchTransaction.getQuestion().getRawInputParameters(),
                    RequestParameters.EXP, null);
                try {
                    nbOfTerms = Integer.parseInt(exp);
                } catch (Throwable t) {
                    log.warn("Invalid '" + RequestParameters.EXP + "' parameter: '" + exp + "'");
                }
            }
            
            String[] queries = searchTransaction.getQuestion().getQuery().split("\\s");
            List<String> urlsToRemove = new ArrayList<>();
            boolean queryChanged = false;
            for(int i=0; i<queries.length; i++) {
                if (queries[i].startsWith(EXPLORE_PREFIX)) {
                    String url = queries[i].substring(EXPLORE_PREFIX.length());
                    urlsToRemove.add(url);
                    String exploreQuery = generator.getExploreQuery(searchTransaction.getQuestion().getCollection(),
                        url, nbOfTerms);
                    if (exploreQuery != null) {
                        queries[i] = exploreQuery;
                        queryChanged = true;
                    } else {
                        log.warn("No explore query returned for URL '" + url + "'");
                    }
                }                 
            }
            
            if (queryChanged) {
                new RemoveUrls(urlsToRemove).performAction(searchTransaction, Action.Phase.INPUT);
                searchTransaction.getQuestion().getDynamicQueryProcessorOptions().add(OPT_VSIMPLE);
                log.debug("Query updated from '" + searchTransaction.getQuestion().getQuery()
                    + "' to '" + StringUtils.join(queries, " ") + "'");
                searchTransaction.getQuestion().setQuery(StringUtils.join(queries, " "));
            }
        }

    }

}
