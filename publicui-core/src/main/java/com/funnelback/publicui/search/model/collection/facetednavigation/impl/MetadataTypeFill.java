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
 * <p>@link CategoryDefinition} based on a metadata (Not a metadata class,
 * but an actual metadata like <tt>dc.data</tt>).</p>
 * 
 * <p>Assumes the metadata has been automatically mapped to a specific field.</p>
 * 
 * @since 11.0
 */
public class MetadataTypeFill extends CategoryDefinition implements MetadataBasedCategory {
	
	/** Automatically assigned metadata class */
	@Getter @Setter private String metafield;
	
	/** {@inheritDoc} */
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
	
	/** {@inheritDoc} */
	@Override
	public String getQueryStringParamName() {
		return RequestParameters.FACET_PREFIX + facetName + CategoryDefinition.QS_PARAM_SEPARATOR + data;
	}

	/** {@inheritDoc} */
	@Override
	public boolean matches(String value, String extraParams) {
		return data.equals(extraParams);
	}

	/** {@inheritDoc} */
	@Override
	public String getMetadataClass() {
		return metafield;
	}

	/** {@inheritDoc} */
	@Override
	public String getQueryConstraint(String value) {
		return metafield + ":\"" + MetadataBasedCategory.INDEX_FIELD_BOUNDARY + " "
				+ value + " "
				+ MetadataBasedCategory.INDEX_FIELD_BOUNDARY + "\"";
	}
}
