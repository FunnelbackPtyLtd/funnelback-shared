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
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;

/**
 * {@link CategoryDefinition} based on a GScope number.
 * 
 * @since 11.0
 */
public class GScopeItem extends CategoryDefinition implements GScopeBasedCategory {

	/** GScope number */
	@Getter @Setter private int userSetGScope;
	
	/** {@inheritDoc} */
	@Override
	@SneakyThrows(UnsupportedEncodingException.class)
	public List<CategoryValue> computeValues(final SearchTransaction st) {
		List<CategoryValue> categories = new ArrayList<CategoryValue>();
		ResultPacket rp = st.getResponse().getResultPacket();
		if (rp.getGScopeCounts().get(userSetGScope) != null) {
			categories.add(new CategoryValue(
					Integer.toString(userSetGScope),
					data,
					rp.getGScopeCounts().get(userSetGScope),
					getQueryStringParamName() + "=" + URLEncoder.encode(data, "UTF-8"),
					Integer.toString(getGScopeNumber())));
		}
		return categories;
	}

	/** {@inheritDoc} */
	@Override
	public String getQueryStringParamName() {
		return RequestParameters.FACET_PREFIX + facetName + CategoryDefinition.QS_PARAM_SEPARATOR + userSetGScope;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean matches(String value, String extraParams) {
		return data.equals(value);
	}

	/** {@inheritDoc} */
	@Override
	public int getGScopeNumber() {
		return userSetGScope;
	}

	/** {@inheritDoc} */
	@Override
	public String getGScope1Constraint() {
		return Integer.toString(userSetGScope);
	}
}
