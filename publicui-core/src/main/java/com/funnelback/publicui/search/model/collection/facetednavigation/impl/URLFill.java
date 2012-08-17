package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;

import org.apache.commons.lang.StringUtils;

import com.funnelback.common.utils.VFSURLUtils;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.MetadataBasedCategory;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * <p>{@link CategoryDefinition} based on an URL prefix.<p>
 * 
 * <p>Every different URL after the prefix will generate a new value.</p>
 * 
 * @since 11.0
 */
@Log4j
public class URLFill extends CategoryDefinition implements MetadataBasedCategory {

	/** Identifier used in query string parameter. */
	private static final String TAG = "url";
	
	/** URLs are indexed in the metadata class <tt>v</tt> */
	private static final String MD = "v";
	
	/** {@inheritDoc} */
	@Override
	@SneakyThrows(UnsupportedEncodingException.class)
	public List<CategoryValue> computeValues(final SearchTransaction st) {
		List<CategoryValue> categories = new ArrayList<CategoryValue>();

		// Strip 'http://' prefixes as PADRE strips them.
		String url = data.replaceFirst("^http://", "");

		// Find out which URL is currently selected. By default
		// it's the root folder specified in the faceted nav. config.
		String currentConstraint = url;
		List<String> currentConstraints = st.getQuestion().getSelectedCategoryValues().get(getQueryStringParamName());
		if (currentConstraints != null) {
			if (currentConstraints.size() > 1) {
				log.warn("More than one URL constraint was selected: '"+StringUtils.join(currentConstraints, ",")+"'. This is not supported, only '"
						+ currentConstraints.get(0) + "' will be considered");
			}
			currentConstraint = currentConstraints.get(0);
		}
		
		if (url.startsWith(VFSURLUtils.WINDOWS_URL_PREFIX)) {
			// Windows style url \\server\share\folder\file.ext
			// Convert to smb://... so that URLs returned by PADRE will match
			url = VFSURLUtils.SystemUrlToVFSUrl(url);
		}
		
		if (currentConstraint.startsWith(VFSURLUtils.WINDOWS_URL_PREFIX)) {
			currentConstraint = VFSURLUtils.SystemUrlToVFSUrl(currentConstraint);
		}
		
		// Comparisons are case-insensitive
		url = url.toLowerCase();
		currentConstraint = currentConstraint.toLowerCase();
		
		for (Entry<String, Integer> entry: st.getResponse().getResultPacket().getUrlCounts().entrySet()) {
			// Do not toLowerCase() here, we still want the original data from Padre
			// with the correct case to display
			String item = entry.getKey().replaceFirst("^http://", "");
			int count = entry.getValue();
			
			if (item.toLowerCase().startsWith(url)
					&& isOneLevelDeeper(currentConstraint, item.toLowerCase())) {
				// 'v' metadata value is the URI only, without
				// the host or protocol.
				String vValue = item.replaceFirst("(\\w+://)?[^/]*/", "");
				
				item = item.substring(url.length());

				// Display only the folder name
				String label = item.substring(item.lastIndexOf('/')+1);
				categories.add(new CategoryValue(
						item,
						label,
						count,
						getQueryStringParamName() + "=" + URLEncoder.encode(vValue, "UTF-8"),
						getMetadataClass()));
			}
		}
		return categories;
	}
	
	/**
	 * Checks if an URL is contained in the current constraints and is only
	 * one level deeper than the current constraints.
	 *  
	 * @param currentConstraint Currently constraint URL, such as <tt>folder1/folder2/</tt>
	 * @param checkUrl URL to check, absolute (Ex: <tt>smb://server/folder/folder2/file3.txt</tt>)
	 * @return true if the URL is in the current constraints, false otherwise
	 */
	private boolean isOneLevelDeeper(String currentConstraint, String checkUrl) {
		if (currentConstraint == null || checkUrl == null) {
			return false;
		}
		
		int i = checkUrl.indexOf(currentConstraint);
		if (i > -1) {
			// Remove head + currentConstraint
			String part = checkUrl.substring(i + currentConstraint.length());
			if (part.startsWith("/")) { part = part.substring(1); }
			return (part.length() > 0 && StringUtils.countMatches(part, "/") < 1);
		}
		
		return false;		
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
		return  MD + ":\"" + value + "\"";
	}

	/** {@inheritDoc} */
	@Override
	public void setData(String data) {
		// Always ensure a trailing slash for the facet
		// breadcrumb to work correctly
		if (data.endsWith("/")) {
			super.setData(data);
		} else {
			super.setData(data+"/");
		}
	}
}
