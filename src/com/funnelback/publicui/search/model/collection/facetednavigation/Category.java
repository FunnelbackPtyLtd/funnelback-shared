package com.funnelback.publicui.search.model.collection.facetednavigation;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.funnelback.publicui.search.model.padre.ResultPacket;

public abstract class Category {

	@Getter @Setter protected String data;
	@Getter private final List<Category> subCategories = new ArrayList<Category>();
	
	/**
	 * Fill this category with data, coming from the {@link ResultPacket}
	 * @param rp
	 */
	public abstract List<com.funnelback.publicui.search.model.transaction.Facet.Category> computeValues(final ResultPacket rp);
	
}
