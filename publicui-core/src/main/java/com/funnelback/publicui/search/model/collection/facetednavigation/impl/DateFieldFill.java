package com.funnelback.publicui.search.model.collection.facetednavigation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.funnelback.common.padre.QueryProcessorOptionKeys;
import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryValueComputedDataHolder;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.MetadataBasedCategory;
import com.funnelback.publicui.search.model.facetednavigation.FacetSelectedDetails;
import com.funnelback.publicui.search.model.padre.DateCount;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.FacetedNavigationUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * <p>{@link CategoryDefinition} based on a metadata class
 * containing date values.</p>
 * 
 * <p>Will generate multiple values for each value of this metadata class.</p>
 * 
 * @since 12.0
 */

public class DateFieldFill extends CategoryDefinition implements MetadataBasedCategory {

    private final List<QueryProcessorOption<?>> qpOptions;
    
    /**
     * This separates the query constraint e.g. d=2016 from the label e.g. "2016"
     * 
     * The separator must be one that does not appear in the query constraint
     * 
     * We can not use ascii control chars like 29 (group separator) because XML 1.0 will throw a fit
     * and not all programs work with XML 1.1 for example xmllint. Instead we will just make a separator
     * that I hope will not be in the constraint.
     *
     * 
     */
    public static final String CONSTRAINT_AND_LABEL_SEPARATOR = " :: ";
    
    private static final Pattern CONSTRAINT_AND_LABEL_SEPERATOR_PATTERN = Pattern.compile(CONSTRAINT_AND_LABEL_SEPARATOR);
    
    
    public DateFieldFill(String metaDataClass) {
        super(metaDataClass);
        qpOptions = Collections.singletonList(new QueryProcessorOption<String>(QueryProcessorOptionKeys.COUNT_DATES, getMetadataClass()));
    }

    /** {@inheritDoc} */
    @Override
    public List<CategoryValueComputedDataHolder> computeData(final SearchTransaction st, FacetDefinition facetDefinition) {
        List<CategoryValueComputedDataHolder> categories = new ArrayList<>();
        
        FacetSearchData facetData = getFacetSearchData(st, facetDefinition);
        
        List<String> selectedQueryStringValues = getMatchingFacetSelectedDetails(st.getQuestion())
                .stream()
                .map(FacetSelectedDetails::getValue)
                .collect(Collectors.toList());
        
        // For each metadata count <rmc item="a:new south wales">42</rmc>
        for (Entry<String, DateCount> entry : facetData.getResponseForValues().getResultPacket().getDateCounts().entrySet()) {
            String item = entry.getKey();
            DateCount dc = entry.getValue();
            MetadataAndValue mdv = parseMetadata(item);
            if (this.data.equals(mdv.metadataClass)) {
                String label = mdv.value;
                String queryStringParamValue = constructCGIValue(dc.getQueryTerm(), label);
                selectedQueryStringValues.remove(queryStringParamValue);
                Integer count = facetData.getResponseForCounts().apply(this, mdv.value)
                        .map(SearchResponse::getResultPacket)
                        .map(ResultPacket::getDateCounts)
                        .map(dateCounts -> dateCounts.get(entry.getKey()))
                        .map(DateCount::getCount)
                        .orElse(facetData.getCountIfNotPresent().apply(this, mdv.value));
                boolean selected = FacetedNavigationUtils.isCategorySelected(this, st.getQuestion().getSelectedCategoryValues(), queryStringParamValue);
                categories.add(makeValue(label, count, selected, queryStringParamValue));
            }
        }
        
        for(String selectQueryStringParamValue : selectedQueryStringValues) {
            String label = constraintAndLabelFromCGIValue(selectQueryStringParamValue).getLabel();
            categories.add(makeValue(label, 0, true, selectQueryStringParamValue));
        }
        
        return categories;
    }
    
    
    private CategoryValueComputedDataHolder makeValue(String label, Integer count, boolean selected, String queryStringParamValue) {
        return new CategoryValueComputedDataHolder(
            label, //
            label,
            count,
            getMetadataClass(),
            selected,
            getQueryStringParamName(),
            queryStringParamValue);
    }
    

    /** {@inheritDoc} */
    @Override
    public String getQueryStringCategoryExtraPart() {
        return data;
    }

    /** {@inheritDoc} */
    @Override
    public boolean matches(String value, String extraParams) {
        return data.equals(extraParams);
    }

    /** {@inheritDoc} */
    @Override
    public String getMetadataClass() {
        return data;
    }

    /**
     * The passed-in value is already a date constraint, so return
     * it as-is.
     * 
     * @param value A date constraint, e.g. <code>d&lt;10Jan2015</code>
     */
    @Override
    public String getQueryConstraint(String value) {
        return constraintAndLabelFromCGIValue(value).getQueryConstraint();
    }
    
    public ConstraintAndLabel constraintAndLabelFromCGIValue(String value) {
        String[] parts = CONSTRAINT_AND_LABEL_SEPERATOR_PATTERN.split(value, 2);
        if(parts.length == 2) {
            return new ConstraintAndLabel(parts[0], parts[1]);
        }
        return new ConstraintAndLabel(value, value);
    }
    
    /**
     * Constructs the CGI query value from the query constraint e.g. d=2008 and the label e.g. 2008
     * 
     * @param queryConstraint
     * @param label
     * @return
     */
    public String constructCGIValue(String queryConstraint, String label) {
        return queryConstraint + CONSTRAINT_AND_LABEL_SEPARATOR + label;
    }
    
    @AllArgsConstructor
    public static class ConstraintAndLabel {
        @Getter @NonNull private final String queryConstraint;
        @Getter @NonNull private final String label;
    }
    
    @Override
    public List<QueryProcessorOption<?>> getQueryProcessorOptions(SearchQuestion question) {
        return qpOptions;
    }

    @Override
    public boolean allValuesDefinedByUser() {
        return false;
    }

    @Override
    public boolean selectedValuesAreNested() {
        return false;
    }
}
