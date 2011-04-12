package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.Collections;
import java.util.List;

import lombok.extern.apachecommons.Log;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryType;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetedNavigationUtils;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.Category;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * Transforms result metadata/url/gscope counts into proper facet hierarchy
 */
@Component("facetedNavigationOutputProcessor")
@Log
public class FacetedNavigation implements OutputProcessor {

	@Override
	public void processOutput(SearchTransaction searchTransaction) {
		if (SearchTransactionUtils.hasCollection(searchTransaction)
				&& SearchTransactionUtils.hasResponse(searchTransaction)
				&& searchTransaction.getResponse().hasResultPacket()) {
			
			FacetedNavigationConfig config = FacetedNavigationUtils.selectConfiguration(searchTransaction.getQuestion().getCollection(), searchTransaction.getQuestion().getProfile());
			
			if (config != null) {
				for(com.funnelback.publicui.search.model.collection.facetednavigation.Facet f: config.getFacets()) {
					Facet facet = new Facet(f.getName());
					for (final CategoryType ct: f.getCategoryTypes()) {
						facet.getCategoryTypes().add(fillCategoryType(ct, searchTransaction));
					}
					searchTransaction.getResponse().getFacets().add(facet);
				}
			}
		}
	}

	private Facet.CategoryType fillCategoryType(final CategoryType ct, SearchTransaction searchTransaction) {
		// Fill the values for this category
		Facet.CategoryType cType = new Facet.CategoryType(ct.getLabel());
		cType.getCategories().addAll(ct.computeValues(searchTransaction.getResponse().getResultPacket()));
		Collections.sort(cType.getCategories(), new Category.ByCountComparator(true));
	
		// Find out if this category is currently selected
		if (searchTransaction.getQuestion().getSelectedCategories().containsKey(ct.getUrlParamName())) {
			String[] paramName = ct.getUrlParamName().split("\\|");
			final String extraParams;
			if (paramName.length > 1) {
				extraParams = paramName[1];
			} else {
				extraParams = null;
			}
			
			List<String> selectedValues = searchTransaction.getQuestion().getSelectedCategories().get(ct.getUrlParamName());
			boolean valueMatches = CollectionUtils.exists(selectedValues, new Predicate() {
				@Override
				public boolean evaluate(Object o) {
					return ct.matches((String) o, extraParams);
				}
			});
		
			if (valueMatches) {
				for (CategoryType subCategoryType: ct.getSubCategories()) {
					cType.getSubCategoryTypes().add(fillCategoryType(subCategoryType, searchTransaction));
				}
			}
		}
		
		return cType;
		
		

		
	}
	
}
