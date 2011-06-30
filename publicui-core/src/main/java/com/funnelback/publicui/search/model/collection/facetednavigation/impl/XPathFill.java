package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.MetadataBasedCategory;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;

/**
 * {@link CategoryDefinition} filled by an XPath expression.
 * Each different value of the XPath expression will generate a category.
 * Values are stored in an automatically assigned metadata class.
 */
public class XPathFill extends CategoryDefinition implements MetadataBasedCategory {
	
	/**
	 * Automatically assigned metadata class for the values
	 * of the XPath expression.
	 */
	@Getter @Setter private String metafield;
	
	@Override
	@SneakyThrows(UnsupportedEncodingException.class)
	public List<CategoryValue> computeValues(final ResultPacket rp) {
		List<CategoryValue> categories = new ArrayList<CategoryValue>();
		
		// For each metadata count <rmc item="a:new south wales">42</rmc>
		for (Entry<String, Integer> entry: rp.getRmcs().entrySet()) {
			String item = entry.getKey();
			int count = entry.getValue();
			MetadataAndValue mdv = parseMetadata(item);
			
			// If the automatically assigned metafield matches
			if (this.metafield.equals(mdv.metadata)) {
				categories.add(new CategoryValue(
						mdv.value,
						mdv.value,
						count,
						getQueryStringParamName() + "=" + URLEncoder.encode(mdv.value, "UTF-8")));
			}
		}
		return categories;
	}

	@Override
	public String getQueryStringParamName() {
		return RequestParameters.FACET_PREFIX + facetName + CategoryDefinition.QS_PARAM_SEPARATOR + metafield;
	}
	
	@Override
	public boolean matches(String value, String extraParams) {
		return metafield.equals(extraParams);
	}
	
	@Override
	public String getMetadataClass() {
		return metafield;
	}

	@Override
	public String getQueryConstraint(String value) {
		return metafield + ":\"" + MetadataBasedCategory.INDEX_FIED_BOUNDARY + " "
				+ value
				+ " " + MetadataBasedCategory.INDEX_FIED_BOUNDARY + "\"";
	}
}
