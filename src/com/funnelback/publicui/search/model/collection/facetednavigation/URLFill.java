package com.funnelback.publicui.search.model.collection.facetednavigation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.funnelback.publicui.search.model.padre.ResultPacket;

public class URLFill extends CategoryType {
	
	@Override
	public List<com.funnelback.publicui.search.model.transaction.Facet.Category> computeValues(final ResultPacket rp) {
		List<com.funnelback.publicui.search.model.transaction.Facet.Category> categories = new ArrayList<com.funnelback.publicui.search.model.transaction.Facet.Category>();
		String url = data.replaceFirst("^http://", "");
		for (Entry<String, Integer> entry: rp.getUrlCounts().entrySet()) {
			String item = entry.getKey().replaceFirst("^http://", "");
			int count = entry.getValue();
			if (item.startsWith(url)) {
				item = item.substring(url.length());
				categories.add(new com.funnelback.publicui.search.model.transaction.Facet.Category(item, item, count));
			}
		}
		return categories;
	}

}
