package com.funnelback.publicui.search.model.collection.facetednavigation;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.Facet.Category;

/**
 * Type of category (metadata field fill, url fill, xpath fill ...)
 *
 */
public abstract class CategoryType {

	@Getter @Setter protected String data;
	@Getter private final List<CategoryType> subCategories = new ArrayList<CategoryType>();
	
	/**
	 * Generate a list of corresponding {@link Category} by applying
	 * this category type rule over a {@link ResultPacket}
	 * @param rp
	 */
	public abstract List<com.funnelback.publicui.search.model.transaction.Facet.Category> computeValues(final ResultPacket rp);
	
}
