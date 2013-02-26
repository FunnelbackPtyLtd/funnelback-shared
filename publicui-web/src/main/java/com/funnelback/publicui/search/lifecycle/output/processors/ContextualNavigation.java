package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.padre.Category;
import com.funnelback.publicui.search.model.padre.Cluster;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.utils.QueryStringUtils;

/**
 * Process PADRE contextual navigation response:
 * - Clears up "more_link"s to remove the hardcoded script name /search/padre-sw.cgi
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
			
			for (Category category: searchTransaction.getResponse().getResultPacket().getContextualNavigation().getCategories()) {
				// Strips any hardcoded URL such as "/search/padre-sw.cgi?..."
				if (category.getMoreLink() != null) {
					category.setMoreLink(category.getMoreLink().substring(category.getMoreLink().indexOf("?")));
				}
				if (category.getFewerLink() != null) {
					category.setFewerLink(category.getFewerLink().substring(category.getFewerLink().indexOf("?")));
				}
				
				// Sets additional parameters to the URL for logging purposes
				for (Cluster cluster: category.getClusters()) {
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

}
