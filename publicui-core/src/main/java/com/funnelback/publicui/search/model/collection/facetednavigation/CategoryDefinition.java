package com.funnelback.publicui.search.model.collection.facetednavigation;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;

/**
 * Category definition (metadata field fill, url fill, xpath fill ...).
 *
 */
@ToString
public abstract class CategoryDefinition {

	/**
	 * Separator used in PADRE results between a metadata field
	 * and its value (Ex: <tt>&lt;rmc item="a:new south wales"&gt;</tt>)
	 */
	public static final String MD_VALUE_SEPARATOR = ":";

	/**
	 * Separator used in the query string parameters between
	 * a facet name and its specific configuration (metadatafield or
	 * gscope number).
	 * Ex: <tt>f.Location|a=new%20south%20wales</tt>
	 */
	public static final String QS_PARAM_SEPARATOR = "|";
	
	/** Name of the facet containing this category type */
	@Getter @Setter protected String facetName;
	
	/**
	 * Specific data for this category type.
	 * Depending of the actual type, can be a metadata class,
	 * a query expression, etc.
	 */
	@Getter @Setter protected String data;
	
	@Getter @Setter protected String label;
	
	/** List of sub category definitions */
	@Getter private final List<CategoryDefinition> subCategories = new ArrayList<CategoryDefinition>();
	
	/**
	 * Generate a list of corresponding {@link CategoryValue} by applying
	 * this category type rule over a {@link ResultPacket}.
	 * 
	 * Used to generate "displayable" categories values on the UI
	 * from the faceted navigation configuration.
	 * 
	 * The size of the list will depend of the type of the category:
	 * - For "fill" type it will be a multivalued list (metadata field fill, etc.)
	 * - For "item" type it will contain a single value (Gscope item, etc.)
	 * 
	 * @param rp
	 */
	public abstract List<CategoryValue> computeValues(final ResultPacket rp);
	
	/**
	 * @return the name of the query string parameter used
	 * to select this category.
	 * 
	 * Ex: f.By Date|dc.date
	 */
	public abstract String getQueryStringParamName();
	
	/**
	 * Given the value of a query string parameter, and any extra parameters,
	 * whether this category types is relevant for this parameter.
	 * 
	 * For example: f.By Date|dc.date=2010-01-01:
	 * -> value = 2010-01-01
	 * -> extra = dc.date
	 * A category of type "metadata fill" for the "dc.date" metadata should return
	 * true.
	 * 
	 * @param value
	 * @param extraParams
	 * @return
	 */
	public abstract boolean matches(String value, String extraParams);
	
	/**
	 * Parses a String containing a metadata class and a value
	 * such as "x:Red cars", to separated the metadata class from the value
	 * @param item
	 * @return
	 */
	public static MetadataAndValue parseMetadata(String item) {
		int colon = item.indexOf(MD_VALUE_SEPARATOR);
		return new MetadataAndValue(item.substring(0, colon), item.substring(colon + 1));
	}

	@AllArgsConstructor
	public static class MetadataAndValue {
		public String metadata;
		public String value;
	}
	
}
