package com.funnelback.publicui.search.model.collection.facetednavigation;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;

public class GScopeItem extends CategoryType implements GScopeBasedType {
	@Getter @Setter private int userSetGScope;
	
	@Override
	public List<com.funnelback.publicui.search.model.transaction.Facet.Category> computeValues(final ResultPacket rp) {
		List<com.funnelback.publicui.search.model.transaction.Facet.Category> categories = new ArrayList<com.funnelback.publicui.search.model.transaction.Facet.Category>();
		if (rp.getGScopeCounts().get(userSetGScope) != null) {
			categories.add(new com.funnelback.publicui.search.model.transaction.Facet.Category(Integer.toString(userSetGScope), data, rp.getGScopeCounts().get(userSetGScope), getUrlParamName() + "=" + data));
		}
		return categories;
	}

	@Override
	public String getUrlParamName() {
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
