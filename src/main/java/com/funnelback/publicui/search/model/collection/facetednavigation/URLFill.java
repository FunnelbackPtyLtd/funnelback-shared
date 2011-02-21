package com.funnelback.publicui.search.model.collection.facetednavigation;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import lombok.SneakyThrows;
import lombok.extern.apachecommons.Log;

import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;

@Log
public class URLFill extends CategoryType implements MetadataBasedType {

	private static final String TAG = "url";
	
	@Override
	public List<com.funnelback.publicui.search.model.transaction.Facet.Category> computeValues(final ResultPacket rp) {
		List<com.funnelback.publicui.search.model.transaction.Facet.Category> categories = new ArrayList<com.funnelback.publicui.search.model.transaction.Facet.Category>();
		String url = data.replaceFirst("^http://", "");
		for (Entry<String, Integer> entry: rp.getUrlCounts().entrySet()) {
			String item = entry.getKey().replaceFirst("^http://", "");
			int count = entry.getValue();
			if (item.startsWith(url)) {
				item = item.substring(url.length());
				categories.add(new com.funnelback.publicui.search.model.transaction.Facet.Category(item, item, count, getUrlParamName() + "=" + item));
			}
		}
		return categories;
	}

	@Override
	public String getUrlParamName() {
		return RequestParameters.FACET_PREFIX + facetName + "|" + TAG;
	}

	@Override
	public boolean matches(String value, String extraParams) {
		return TAG.equals(extraParams);
	}
	
	@Override
	public String getMetadataClass() {
		return "v";
	}
	
	@Override
	@SneakyThrows(UnsupportedEncodingException.class)
	public String getQueryConstraint(String value) {
		return  "v:" + URLEncoder.encode(value, "UTF-8");
	}
}
