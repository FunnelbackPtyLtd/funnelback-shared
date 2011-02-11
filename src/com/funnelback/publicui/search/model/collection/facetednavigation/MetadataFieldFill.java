package com.funnelback.publicui.search.model.collection.facetednavigation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import lombok.AllArgsConstructor;

import com.funnelback.publicui.search.model.padre.ResultPacket;

public class MetadataFieldFill extends Category {

	@Override
	public List<com.funnelback.publicui.search.model.transaction.Facet.Category> computeValues(final ResultPacket rp) {
		List<com.funnelback.publicui.search.model.transaction.Facet.Category> categories = new ArrayList<com.funnelback.publicui.search.model.transaction.Facet.Category>();
		for (Entry<String, Integer> entry: rp.getRmcs().entrySet()) {
			String item = entry.getKey();
			int count = entry.getValue();
			MetadataAndValue mdv = parseMetadata(item);
			if (this.data.equals(mdv.metadata)) {
				categories.add(new com.funnelback.publicui.search.model.transaction.Facet.Category(mdv.value, mdv.value, count));
			}
		}
		return categories;
	}

	private MetadataAndValue parseMetadata(String item) {
		int colon = item.indexOf(":");
		return new MetadataAndValue(item.substring(0, colon), item.substring(colon+1));
	}
	
	@AllArgsConstructor
	public class MetadataAndValue {
		public String metadata;
		public String value;
	}
	

}
