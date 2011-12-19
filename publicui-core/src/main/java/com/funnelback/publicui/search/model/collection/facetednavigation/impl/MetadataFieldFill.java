package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import lombok.SneakyThrows;

import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.MetadataBasedCategory;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;

/**
 * <p>{@link CategoryDefinition} based on a metadata class.</p>
 * 
 * <p>Will generate multiple values for each value of this metadata class.</p>
 * 
 * @since 11.0
 */
public class MetadataFieldFill extends CategoryDefinition implements MetadataBasedCategory {

	/** {@inheritDoc} */
	@Override
	@SneakyThrows(UnsupportedEncodingException.class)
	public List<CategoryValue> computeValues(final ResultPacket rp) {
		List<CategoryValue> categories = new ArrayList<CategoryValue>();
		
		// For each metadata count <rmc item="a:new south wales">42</rmc>
		for (Entry<String, Integer> entry : rp.getRmcs().entrySet()) {
			String item = entry.getKey();
			int count = entry.getValue();
			MetadataAndValue mdv = parseMetadata(item);
			if (this.data.equals(mdv.metadata)) {
				categories.add(new CategoryValue(
						mdv.value,
						mdv.value,
						count,
						getQueryStringParamName() + "=" + URLEncoder.encode(mdv.value, "UTF-8"),
						getMetadataClass()));
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
		return data;
	}

	/** {@inheritDoc} */
	@Override
	public String getQueryConstraint(String value) {
		return data + ":\""+ MetadataBasedCategory.INDEX_FIELD_BOUNDARY + " "
				+ value + " "
				+ MetadataBasedCategory.INDEX_FIELD_BOUNDARY + "\"";
	}
}
