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
public class MetadataFieldFill extends CategoryType implements MetadataBasedType {

	@Override
	public List<com.funnelback.publicui.search.model.transaction.Facet.Category> computeValues(final ResultPacket rp) {
		List<com.funnelback.publicui.search.model.transaction.Facet.Category> categories = new ArrayList<com.funnelback.publicui.search.model.transaction.Facet.Category>();
		for (Entry<String, Integer> entry : rp.getRmcs().entrySet()) {
			String item = entry.getKey();
			int count = entry.getValue();
			MetadataAndValue mdv = parseMetadata(item);
			if (this.data.equals(mdv.metadata)) {
				categories.add(new com.funnelback.publicui.search.model.transaction.Facet.Category(mdv.value,
						mdv.value, count, getUrlParamName() + "=" + mdv.value));
			}
		}
		return categories;
	}

	@Override
	public String getUrlParamName() {
		return RequestParameters.FACET_PREFIX + facetName + "|" + data;
	}

	@Override
	public boolean matches(String value, String extraParams) {
		return data.equals(extraParams);
	}

	@Override
	public String getMetadataClass() {
		return data;
	}

	@Override
	@SneakyThrows(UnsupportedEncodingException.class)
	public String getQueryConstraint(String value) {
		return data + ":\"" + URLEncoder.encode("$++ " + value + " $++", "UTF-8") + "\"";
	}
}
