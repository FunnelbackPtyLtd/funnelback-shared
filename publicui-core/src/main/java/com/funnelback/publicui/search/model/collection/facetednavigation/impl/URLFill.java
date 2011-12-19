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
 * <p>{@link CategoryDefinition} based on an URL prefix.<p>
 * 
 * <p>Every different URL after the prefix will generate a new value.</p>
 * 
 * @since 11.0
 */
public class URLFill extends CategoryDefinition implements MetadataBasedCategory {

	/** Identifier used in query string parameter. */
	private static final String TAG = "url";
	
	/** URLs are indexed in the metadata class <tt>v</tt> */
	private static final String MD = "v";
	
	/** {@inheritDoc} */
	@Override
	@SneakyThrows(UnsupportedEncodingException.class)
	public List<CategoryValue> computeValues(final ResultPacket rp) {
		List<CategoryValue> categories = new ArrayList<CategoryValue>();
		
		// Strip 'http://' prefixes as PADRE strips them.
		String url = data.replaceFirst("^http://", "");
		for (Entry<String, Integer> entry: rp.getUrlCounts().entrySet()) {
			String item = entry.getKey().replaceFirst("^http://", "");
			int count = entry.getValue();
			if (item.startsWith(url)) {
				// 'v' metadata value is the URI only, without
				// the host.
				String vValue = item.replaceFirst("[^/]*/", "");
				
				item = item.substring(url.length());				
				categories.add(new CategoryValue(
						item,
						item,
						count,
						getQueryStringParamName() + "=" + URLEncoder.encode(vValue, "UTF-8"),
						getMetadataClass()));
			}
		}
		return categories;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>Note that this category definition will use a special
	 * tag <tt>url</tt> instead of directly the metadata <tt>v</tt> on which
	 * URLs are mapped internally. For example: <tt>f.Category|url</tt> instead
	 * of <tt>f.Category|v</tt>.</p>
	 * 
	 * <p><tt>v</tt> can't be used otherwise we won't be able to distinguish
	 * between an <em>URL</em> type category definition and a <em>Metadata field</em>
	 * type definition.</p>
	 */
	@Override
	public String getQueryStringParamName() {
		return RequestParameters.FACET_PREFIX + facetName + CategoryDefinition.QS_PARAM_SEPARATOR + TAG;
	}

	/** {@inheritDoc} */
	@Override
	public boolean matches(String value, String extraParams) {
		return TAG.equals(extraParams);
	}
	
	/** {@inheritDoc} */
	@Override
	public String getMetadataClass() {
		return MD;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getQueryConstraint(String value) {
		return  MD + ":" + value;
	}
}
