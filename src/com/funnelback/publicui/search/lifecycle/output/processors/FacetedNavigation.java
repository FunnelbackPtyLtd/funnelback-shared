package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.Collections;
import java.util.Comparator;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetedNavigationUtils;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.Category;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Transforms result metadata counts into proper facet hierarchy
 */
@Component("facetedNavigationOutputProcessor")
public class FacetedNavigation implements OutputProcessor {

	@Override
	public void process(SearchTransaction searchTransaction) {
		if (SearchTransactionUtils.hasResponse(searchTransaction)
				&& searchTransaction.getResponse().hasResultPacket()) {
			
			FacetedNavigationConfig config = FacetedNavigationUtils.selectConfiguration(searchTransaction.getQuestion().getCollection(), searchTransaction.getQuestion().getProfile());
			
			if (config != null) {
				for(com.funnelback.publicui.search.model.collection.facetednavigation.Facet f: config.getFacets()) {
					Facet facet = new Facet(f.getName());
					for (com.funnelback.publicui.search.model.collection.facetednavigation.Category c: f.getCategories()) {
						facet.getCategories().addAll(c.computeValues(searchTransaction.getResponse().getResultPacket()));
						Collections.sort(facet.getCategories(), new ByCountComparator(true));
					}
					searchTransaction.getResponse().getFacets().add(facet);
				}
				
			}
		}
	}
	
	public class ByCountComparator implements Comparator<Facet.Category> {
		private boolean reverse;
		
		public ByCountComparator(boolean reverse) {
			this.reverse = reverse;
		}
		
		@Override
		public int compare(Category c1, Category c2) {
			if (reverse) {
				return c2.getCount() - c1.getCount();
			} else {
				return c1.getCount() - c2.getCount();
			}
		}

		
	}
	
}
