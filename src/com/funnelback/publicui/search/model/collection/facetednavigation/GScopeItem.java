package com.funnelback.publicui.search.model.collection.facetednavigation;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.funnelback.publicui.search.model.padre.ResultPacket;

public class GScopeItem extends CategoryType {
	@Getter @Setter private int userSetGScope;
	
	@Override
	public List<com.funnelback.publicui.search.model.transaction.Facet.Category> computeValues(final ResultPacket rp) {
		List<com.funnelback.publicui.search.model.transaction.Facet.Category> categories = new ArrayList<com.funnelback.publicui.search.model.transaction.Facet.Category>();
		categories.add(new com.funnelback.publicui.search.model.transaction.Facet.Category(Integer.toString(userSetGScope), data, rp.getGScopeCounts().get(userSetGScope)));
		return categories;
	}
	
}
