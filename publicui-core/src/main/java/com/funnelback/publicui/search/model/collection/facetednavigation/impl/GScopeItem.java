package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.common.padre.QueryProcessorOptionKeys;
import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryValueComputedDataHolder;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.GScopeBasedCategory;
import com.funnelback.publicui.search.model.facetednavigation.FacetSelectedDetailts;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
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
    @Getter @Setter private String userSetGScope;
    
    public GScopeItem(String categoryName, String userSetGscope) {
        super(categoryName);
        this.userSetGScope = userSetGscope;
        qpOptions = Collections.singletonList(new QueryProcessorOption<String>(QueryProcessorOptionKeys.COUNTGBITS, "all"));
    }

    /** {@inheritDoc} */
    @Override
    public List<CategoryValueComputedDataHolder> computeData(final SearchTransaction st, FacetDefinition facetDefinition) {
        
        FacetSearchData facetData = getFacetSearchData(st, facetDefinition);
        
        List<CategoryValueComputedDataHolder> categories = new ArrayList<>();
        
        boolean hasValue = Optional.ofNullable(facetData.getResponseForValues())
            .map(SearchResponse::getResultPacket)
            .map(ResultPacket::getGScopeCounts)
            .map(m -> m.get(userSetGScope))
            .map(count -> count > 0)
            .orElse(facetDefinition.getFacetValues() == FacetValues.FROM_UNSCOPED_ALL_QUERY);
        if (hasValue) {
            
            Integer count = facetData.getResponseForCounts().apply(this, userSetGScope)
                    .map(SearchResponse::getResultPacket)
                    .map(ResultPacket::getGScopeCounts)
                    .map(gscopeCounts -> gscopeCounts.get(userSetGScope))
                    .orElse(facetData.getCountIfNotPresent().apply(this, userSetGScope));
            
            categories.add(makeValue(
                FacetedNavigationUtils.isCategorySelected(this, st.getQuestion().getSelectedCategoryValues(), data), 
                count)
                );
        } else {
            // Even if we don't have a value it may be selected so we need to fake it.
            List<FacetSelectedDetailts> facetParams = getMatchingFacetSelectedDetails(st.getQuestion());
            // Its not empty so we need to fake add a value.
            if(!facetParams.isEmpty()) {
                categories.add(makeValue(true, 0));
            }
        }
        
        return categories;
    }
    
    private CategoryValueComputedDataHolder makeValue(boolean selected, Integer count) {
        return new CategoryValueComputedDataHolder(
            userSetGScope,
            data,
            count,
            getGScopeNumber(),
            selected,
            getQueryStringParamName(),
            data
            );
    }

    /** {@inheritDoc} */
    @Override
    public String getQueryStringCategoryExtraPart() {
        return userSetGScope;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean matches(String value, String extraParams) {
        return data.equals(value) && userSetGScope.equals(extraParams);
    }

    /** {@inheritDoc} */
    @Override
    public String getGScopeNumber() {
        return userSetGScope;
    }

    /** {@inheritDoc} */
    @Override
    public String getGScope1Constraint() {
        return userSetGScope;
    }
    
    @Override
    public List<QueryProcessorOption<?>> getQueryProcessorOptions(SearchQuestion question) {
        return qpOptions;
    }

    @Override
    public boolean allValuesDefinedByUser() {
        return true;
    }

    @Override
    public boolean selectedValuesAreNested() {
        return false;
    }
    
    @Override
    public String toString() {
        return "Label=" + getData() + " gscope=" + getUserSetGScope();
    }
}
