package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.GScopeBasedCategory;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.Facet.Category;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;

/**
 * {@link CategoryDefinition} based on a GScope number
 * @author Administrator
 *
 */
public class GScopeItem extends CategoryDefinition implements GScopeBasedCategory {
	/** GScope number */
	@Getter @Setter private int userSetGScope;
	
	@Override
	public List<Category> computeValues(final ResultPacket rp) {
		List<Category> categories = new ArrayList<Category>();
		if (rp.getGScopeCounts().get(userSetGScope) != null) {
			categories.add(new Category(
					Integer.toString(userSetGScope),
					data,
					rp.getGScopeCounts().get(userSetGScope),
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
		return userSetGScope;
	}

	@Override
	public String getGScope1Constraint() {
		return Integer.toString(userSetGScope);
	}
}
