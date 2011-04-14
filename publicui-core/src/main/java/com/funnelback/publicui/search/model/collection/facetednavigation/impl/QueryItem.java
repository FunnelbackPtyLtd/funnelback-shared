package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.ListUtils;

import lombok.Getter;
import lombok.Setter;

import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.GScopeBasedCategory;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.Facet.Category;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;

/**
 * {@link CategoryDefinition} based on a Query.
 * The query will automatically generate a gscope for the document matching it.
 */
public class QueryItem extends CategoryDefinition implements GScopeBasedCategory {
	
	/**
	 * Query expression for this category
	 */
	@Getter @Setter private String query;
	
	/**
	 * Automatically assigned GScope field.
	 */
	@Getter @Setter private int gscopefield;
	
	@Override
	public List<Category> computeValues(final ResultPacket rp) {
		List<Category> categories = new ArrayList<Category>();
		if (rp.getGScopeCounts().get(gscopefield) != null) {
			categories.add(new Category(
					Integer.toString(gscopefield),
					data,
					rp.getGScopeCounts().get(gscopefield),
					getQueryStringParamName() + "=" + data));
		}
		return categories;
	}
	
	@Override
	public String getQueryStringParamName() {
		return RequestParameters.FACET_PREFIX + facetName;
	}
	
	@Override
	public boolean matches(String value, String extraParams) {
		return data.equals(value);
	}
	
	@Override
	public int getGScopeNumber() {
		return gscopefield;
	}

	@Override
	public String getGScope1Constraint() {
		return Integer.toString(gscopefield);
	}
}
