package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.stereotype.Component;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.Category;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * <p>Applies white and black list to faceted navigation categories.</p>
 */
@Component("facetedNavigationWhiteBlackListOutputProcessor")
@CommonsLog
public class FacetedNavigationWhiteBlackList implements OutputProcessor {

	@Override
	public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
		if (SearchTransactionUtils.hasCollection(searchTransaction)
				&& SearchTransactionUtils.hasResponse(searchTransaction)
				&& searchTransaction.getResponse().getFacets().size() > 0) {
			
			Config config = searchTransaction.getQuestion().getCollection().getConfiguration();
			for (Facet f: searchTransaction.getResponse().getFacets()) {
				if (config.hasValue(Keys.FacetedNavigation.WHITE_LIST+"."+f.getName())) {
					applyWhiteList(f.getCategories(),
						config.value(Keys.FacetedNavigation.WHITE_LIST+"."+f.getName()).toLowerCase().split(","));
					
					log.debug("Applied white list '"+config.value(Keys.FacetedNavigation.WHITE_LIST+"."+f.getName())+ "'"
							+ " to facet '" + f.getName() + "'");
				}
			
				if (config.hasValue(Keys.FacetedNavigation.BLACK_LIST+"."+f.getName())) {
					applyBlackList(f.getCategories(),
						config.value(Keys.FacetedNavigation.BLACK_LIST+"."+f.getName()).toLowerCase().split(","));
					
					log.debug("Applied black list '"+config.value(Keys.FacetedNavigation.BLACK_LIST+"."+f.getName())+"'"
							+ " to facet '" + f.getName() + "'");
				}
				
			}
		}
	}

	/**
	 * <p>Applies a white list recursively to facets categories values.</p>
	 * <p>Remove any value not contained in the white list.</p>
	 * @param categories
	 * @param whiteList
	 */
	private void applyWhiteList(final List<Category> categories, final String[] whiteList) {
		for (Category c: categories) {
			CollectionUtils.filter(c.getValues(), new Predicate() {
				@Override
				public boolean evaluate(Object object) {
					CategoryValue cv = (CategoryValue) object;
					return ArrayUtils.contains(whiteList, cv.getLabel().toLowerCase());
				}
			});
			applyWhiteList(c.getCategories(), whiteList);
		}
	}
	
	/**
	 * <p>Applies a black list recursively to facets categories values.</p>
	 * <p>Remove any value contained in the black list.</p>
	 * @param categories
	 * @param blackList
	 */
	private void applyBlackList(final List<Category> categories, final String[] blackList) { 
		for (Category c: categories) {
			CollectionUtils.filter(c.getValues(), new Predicate() {
				@Override
				public boolean evaluate(Object object) {
					CategoryValue cv = (CategoryValue) object;
					return ! ArrayUtils.contains(blackList, cv.getLabel().toLowerCase());
				}
			});
			applyBlackList(c.getCategories(), blackList);
		}	
	}
}
