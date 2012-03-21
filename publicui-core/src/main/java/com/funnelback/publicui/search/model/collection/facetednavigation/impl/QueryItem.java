package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.GScopeBasedCategory;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;

/**
 * <p>{@link CategoryDefinition} based on a query.</p>
 * 
 * <p>The query will automatically generate a GScope for the document matching it.</p>
 * 
 * @since 11.0
 */
public class QueryItem extends CategoryDefinition implements GScopeBasedCategory {
	
	/** Query expression for this category */
	@Getter @Setter private String query;
	
	/** Automatically assigned GScope field. */
	@Getter @Setter private int gscopefield;
	
	/** {@inheritDoc} */
	@Override
	@SneakyThrows(UnsupportedEncodingException.class)
	public List<CategoryValue> computeValues(final ResultPacket rp) {
		List<CategoryValue> categories = new ArrayList<CategoryValue>();
		if (rp.getGScopeCounts().get(gscopefield) != null) {
			categories.add(new CategoryValue(
					Integer.toString(gscopefield),
					data,
					rp.getGScopeCounts().get(gscopefield),
					getQueryStringParamName() + "=" + URLEncoder.encode(data, "UTF-8"),
					Integer.toString(getGScopeNumber())));
		}
		return categories;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getQueryStringParamName() {
		return RequestParameters.FACET_PREFIX + facetName + CategoryDefinition.QS_PARAM_SEPARATOR + gscopefield;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean matches(String value, String extraParams) {
		return data.equals(value);
	}
	
	/** {@inheritDoc} */
	@Override
	public int getGScopeNumber() {
		return gscopefield;
	}

	/** {@inheritDoc} */
	@Override
	public String getGScope1Constraint() {
		return Integer.toString(gscopefield);
	}
}
