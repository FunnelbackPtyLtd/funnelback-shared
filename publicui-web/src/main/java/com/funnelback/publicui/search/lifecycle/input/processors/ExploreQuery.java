package com.funnelback.publicui.search.lifecycle.input.processors;

import javax.servlet.http.HttpServletRequest;

import lombok.Setter;
import lombok.extern.apachecommons.Log;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.InputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.explore.ExploreQueryGenerator;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Processes explore:... queries. Calls padre-rf to
 * get query terms and replace the explore:... query with those.
 */
@Component("exploreQueryInputProcessor")
@Log
public class ExploreQuery implements InputProcessor {

	private static final String OPT_VSIMPLE = "-vsimple";
	private static final String OPT_DAAT0 = "-daat0";
	
	private final String EXPLORE_PREFIX = "explore:";
	
	@Autowired
	@Setter private ExploreQueryGenerator generator;
	
	@Override
	public void process(SearchTransaction searchTransaction, HttpServletRequest request) throws InputProcessorException {
		if (SearchTransactionUtils.hasQueryAndCollection(searchTransaction) && request != null) {
			
			Integer nbOfTerms = null;
			if (request.getParameter(RequestParameters.EXP) != null) {
				try {
					nbOfTerms = Integer.parseInt(request.getParameter(RequestParameters.EXP));
				} catch (Throwable t) {
					log.warn("Invalid '" + RequestParameters.EXP + "' parameter: '" + request.getParameter(RequestParameters.EXP) + "'");
				}
			}
			
			String[] queries = searchTransaction.getQuestion().getQuery().split("\\s");
			boolean queryChanged = false;
			for(int i=0; i<queries.length; i++) {
				if (queries[i].startsWith(EXPLORE_PREFIX)) {
					String url = queries[i].substring(EXPLORE_PREFIX.length());
					String exploreQuery = generator.getExploreQuery(searchTransaction.getQuestion().getCollection(), url, nbOfTerms);
					if (exploreQuery != null) {
						queries[i] = exploreQuery;
						queryChanged = true;
					} else {
						log.warn("No explore query returned for URL '" + url + "'");
					}
				} 				
			}
			
			if (queryChanged) {
				searchTransaction.getQuestion().getDynamicQueryProcessorOptions().add(OPT_VSIMPLE);
				searchTransaction.getQuestion().getDynamicQueryProcessorOptions().add(OPT_DAAT0);
				log.debug("Query updated from '" + searchTransaction.getQuestion().getQuery() + "' to '" + StringUtils.join(queries, " ") + "'");
				searchTransaction.getQuestion().setQuery(StringUtils.join(queries, " "));
			}
		}

	}

}
