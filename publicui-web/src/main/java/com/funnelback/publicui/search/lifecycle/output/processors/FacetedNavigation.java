package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.Collections;
import java.util.List;

import lombok.extern.apachecommons.Log;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.Category;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.publicui.utils.FacetedNavigationUtils;

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
				for(FacetDefinition f: config.getFacetDefinitions()) {
					Facet facet = new Facet(f.getName());
					for (final CategoryDefinition ct: f.getCategoryDefinitions()) {
						facet.getCategoryTypes().add(fillCategoryType(ct, searchTransaction));
					}
					searchTransaction.getResponse().getFacets().add(facet);
				}
			}
		}
	}

	/**
	 * Fills the categories values for a given {@link CategoryDefinition}, by computing all the
	 * values from the result packet (Delegated to the {@link CategoryDefinition} implementation.
	 * 
	 * If this category has been selected by the user, fills the sub-categories.
	 * 
	 * @param ct
	 * @param searchTransaction
	 * @return
	 */
	private Facet.CategoryType fillCategoryType(final CategoryDefinition ct, SearchTransaction searchTransaction) {
		log.trace("Filling category '" + ct + "'");
		
		// Fill the values for this category
		Facet.CategoryType cType = new Facet.CategoryType(ct.getLabel());
		cType.getCategories().addAll(ct.computeValues(searchTransaction.getResponse().getResultPacket()));
		Collections.sort(cType.getCategories(), new Category.ByCountComparator(true));
	
		// Find out if this category is currently selected
		if (searchTransaction.getQuestion().getSelectedCategories().containsKey(ct.getQueryStringParamName())) {
			// Something has been selected (f.FacetName|extraparm=value)
			String[] paramName = ct.getQueryStringParamName().split("\\" + CategoryDefinition.QS_PARAM_SEPARATOR);
			
			// Find out the extra param (Usually a MD letter, or a gscope number
			final String extraParam;
			if (paramName.length > 1) {
				extraParam = paramName[1];
			} else {
				extraParam = null;
			}
			log.trace("Category has been selected. Extra param is '" + extraParam + "'");
			
			// Find out selected category(ies)
			List<String> selectedValues = searchTransaction.getQuestion().getSelectedCategories().get(ct.getQueryStringParamName());
			boolean valueMatches = CollectionUtils.exists(selectedValues, new Predicate() {
				@Override
				public boolean evaluate(Object o) {
					return ct.matches((String) o, extraParam);
				}
			});
		
			if (valueMatches) {
				log.trace("Value has been selected.");
				for (CategoryDefinition subCategoryType: ct.getSubCategories()) {
					cType.getSubCategoryTypes().add(fillCategoryType(subCategoryType, searchTransaction));
				}
			}
		}		
		return cType;
	}

}
