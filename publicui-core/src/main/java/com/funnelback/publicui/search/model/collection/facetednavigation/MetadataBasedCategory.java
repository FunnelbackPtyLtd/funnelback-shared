package com.funnelback.publicui.search.model.collection.facetednavigation;

/**
 * A {@link CategoryDefinition} based on a metadata class
 */
public interface MetadataBasedCategory {

	/**
	 * Special word indexed at the start and end of each metadata
	 * @see padre-iw's <tt>-ifb</tt> option
	 */
	public static final String INDEX_FIED_BOUNDARY = "$++";
	
	/**
	 * @return The metadata class for this category type
	 */
	public String getMetadataClass();
	
	/**
	 * The specific query constraint to apply for the passed value.
	 * @param value Value for this category
	 * @return The corresponding query constraint (ex: "x:$++ <value> $++", or "v:<value>")
	 */
	public String getQueryConstraint(String value);
	
}
