package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.funnelback.common.padre.QueryProcessorOptionKeys;
import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryValueComputedDataHolder;
import com.funnelback.publicui.search.model.collection.facetednavigation.GScopeBasedCategory;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.FacetedNavigationUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link CategoryDefinition} based on a GScope number.
 * 
 * @since 11.0
 */
@ToString(callSuper=true)
public class GScopeItem extends CategoryDefinition implements GScopeBasedCategory {

    private final List<QueryProcessorOption<?>> qpOptions;
    
    /** GScope number */
    @Getter @Setter private int userSetGScope;
    
    public GScopeItem(String categoryName, long userSetGscope) {
        super(categoryName);
        this.userSetGScope = (int) userSetGscope;
        qpOptions = Collections.singletonList(new QueryProcessorOption<String>(QueryProcessorOptionKeys.COUNTGBITS, "all"));
    }

    /** {@inheritDoc} */
    @Override
    public List<CategoryValueComputedDataHolder> computeData(final SearchTransaction st) {
        List<CategoryValueComputedDataHolder> categories = new ArrayList<>();
        ResultPacket rp = st.getResponse().getResultPacket();
        if (rp.getGScopeCounts().get(userSetGScope) != null) {
            String queryStringParamValue = data;
            categories.add(new CategoryValueComputedDataHolder(
                    Integer.toString(userSetGScope),
                    data,
                    rp.getGScopeCounts().get(userSetGScope),
                    Integer.toString(getGScopeNumber()),
                    FacetedNavigationUtils.isCategorySelected(this, st.getQuestion().getSelectedCategoryValues(), data),
                    getQueryStringParamName(),
                    queryStringParamValue
                    )
                );
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
        return data.equals(value) && Integer.parseInt(extraParams) == userSetGScope;
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
    
    @Override
    public List<QueryProcessorOption<?>> getQueryProcessorOptions(SearchQuestion question) {
        return qpOptions;
    }
}
