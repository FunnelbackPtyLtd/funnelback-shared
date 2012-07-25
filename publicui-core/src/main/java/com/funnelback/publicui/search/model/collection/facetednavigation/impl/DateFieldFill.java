package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import lombok.SneakyThrows;

import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.MetadataBasedCategory;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * <p>{@link CategoryDefinition} based on a metadata class
 * containing date values.</p>
 * 
 * <p>Will generate multiple values for each value of this metadata class.</p>
 * 
 * @since 12.0
 */
public class DateFieldFill extends CategoryDefinition implements MetadataBasedCategory {

	/** {@inheritDoc} */
	@Override
	@SneakyThrows(UnsupportedEncodingException.class)
	public List<CategoryValue> computeValues(final SearchTransaction st) {
		List<CategoryValue> categories = new ArrayList<CategoryValue>();
		
		// For each metadata count <rmc item="a:new south wales">42</rmc>
		for (Entry<String, Integer> entry : st.getResponse().getResultPacket().getDateCounts().entrySet()) {
			String item = entry.getKey();
			int count = entry.getValue();
			// FIXME We assume there's only the 'd' metadata class
			// used here
			categories.add(new CategoryValue(
					"d",
					item,
					count,
					getQueryStringParamName() + "=" + URLEncoder.encode(item, "UTF-8"),
					getMetadataClass()));
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
		try {
			// Year type value
			int year = Integer.parseInt(value);
			return data + "=" + year;
		} catch (Exception e) {
			// Label value "last year", "last week", etc.
			// TODO
			return data + ":" + value;
		}
	}
}
