package com.funnelback.publicui.search.model.collection.facetednavigation;

/**
 * <p>GScope based {@link CategoryDefinition}.</p>
 * 
 * @since 11.0
 */
public interface GScopeBasedCategory {

	/**
	 * Get the GScope number.
	 * @return The GScope number for this category type.
	 */
	public int getGScopeNumber();
	
	/**
	 * Get the GScope constraint.
	 * 
	 * @return The GScope constraint for this category type.
	 * It's actually the same as {@link #getGScopeNumber()}
	 */
	public String getGScope1Constraint();
	
}
