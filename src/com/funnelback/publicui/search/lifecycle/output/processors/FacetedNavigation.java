package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.output.OutputProcessor;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Transforms result metadata counts into proper facet hierarchy
 */
@Component("facetedNavigationOutputProcessor")
public class FacetedNavigation implements OutputProcessor {

	@Override
	public void process(SearchTransaction searchTransaction) {
		if (searchTransaction != null && searchTransaction.hasResponse()
				&& searchTransaction.getResponse().hasResultPacket()
				&& searchTransaction.getResponse().getResultPacket().getRmcs().size() > 0) {
			
			Map<String, Facet> facets = new HashMap<String, Facet>();
			
			for (Entry<String, Integer> entry: searchTransaction.getResponse().getResultPacket().getRmcs().entrySet()) {
				String item = entry.getKey();
				int count = entry.getValue();
				MetadataAndValue mdv = parseMetadata(item);
				Facet f = facets.get(mdv.metadata);
				if (f == null) {
					f = new Facet(mdv.metadata);
					facets.put(mdv.metadata, f);
				}
				f.getCategories().add( (f).new Category(mdv.value, count));
			}
			
			searchTransaction.getResponse().setFacets(new ArrayList<Facet>(facets.values()));
		}
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
