package com.funnelback.publicui.search.model.collection.facetednavigation;

/**
 * A {@link CategoryType} based on a metadata class
 */
public interface MetadataBasedType {

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
