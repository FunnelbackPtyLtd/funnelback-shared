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
import com.funnelback.publicui.search.model.transaction.Facet.Category;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;

/**
 * {@link CategoryDefinition} based on an URL prefix. Every subsequent
 * URL will generate a new value.
 */
public class URLFill extends CategoryDefinition implements MetadataBasedCategory {

	/**
	 * Identifier used in query string parameter.
	 */
	private static final String TAG = "url";
	
	/** URLs are indexed in metadata field 'v' */
	private static final String MD = "v";
	
	@Override
	public List<Category> computeValues(final ResultPacket rp) {
		List<Category> categories = new ArrayList<Category>();
		
		// Strip 'http://' prefixes as PADRE strips them.
		String url = data.replaceFirst("^http://", "");
		for (Entry<String, Integer> entry: rp.getUrlCounts().entrySet()) {
			String item = entry.getKey().replaceFirst("^http://", "");
			int count = entry.getValue();
			if (item.startsWith(url)) {
				item = item.substring(url.length());
				categories.add(new Category(
						item,
						item,
						count,
						getQueryStringParamName() + "=" + item));
			}
		}
		return categories;
	}

	@Override
	public String getQueryStringParamName() {
		// Use a special TAG instead of the metadata class 'v'.
		// We can't really use 'v' here because we need to distinguish
		// between an 'URL' type category, and a 'Metadata field' type
		// category.
		return RequestParameters.FACET_PREFIX + facetName + CategoryDefinition.QS_PARAM_SEPARATOR + TAG;
	}

	@Override
	public boolean matches(String value, String extraParams) {
		return TAG.equals(extraParams);
	}
	
	@Override
	public String getMetadataClass() {
		return MD;
	}
	
	@Override
	@SneakyThrows(UnsupportedEncodingException.class)
	public String getQueryConstraint(String value) {
		return  MD + ":" + URLEncoder.encode(value, "UTF-8");
	}
}
