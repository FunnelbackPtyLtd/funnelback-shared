package com.funnelback.publicui.search.model.collection.facetednavigation;

/**
 * GScope based {@link CategoryType}.
 */
public interface GScopeBasedType {

	/**
	 * @return The GScope number for this category type
	 */
	public int getGScopeNumber();
	
	/**
	 * @return The GScope constraint for this category type.
	 * It's actually the same as {@link #getGScopeNumber()}
	 */
	public String getGScope1Constraint();
	
}
