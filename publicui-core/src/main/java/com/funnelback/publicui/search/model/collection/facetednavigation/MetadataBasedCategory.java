package com.funnelback.publicui.search.model.collection.facetednavigation;

/**
 * A metadata class based {@link CategoryDefinition}.
 * 
 * @since 11.0
 */
public interface MetadataBasedCategory {

	/**
	 * <p>Special word indexed at the start and end of each metadata.</p>
	 * 
	 * <p>See: <tt>padre-iw</tt>'s <tt>-ifb</tt> option.</p>
	 */
	public static final String INDEX_FIELD_BOUNDARY = "$++";
	
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
