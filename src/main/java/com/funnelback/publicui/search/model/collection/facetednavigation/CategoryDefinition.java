package com.funnelback.publicui.search.model.collection.facetednavigation;

import static com.funnelback.publicui.search.model.collection.facetednavigation.FacetExtraSearchNames.SEARCH_FOR_ALL_VALUES;
import static com.funnelback.publicui.search.model.collection.facetednavigation.FacetExtraSearchNames.SEARCH_FOR_UNSCOPED_VALUES;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.funnelback.common.facetednavigation.models.FacetConstraintJoin;
import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.funnelback.publicui.search.model.facetednavigation.FacetSelectedDetails;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.FacetedNavigationUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
/**
 * <p>Category definition for faceted navigation.</p>
 * 
 * <p>A category definition possess a label (Ex: "Cities") and
 * defines the way the values will be generated (Ex: "Take all the
 * existing values of the metadata class X").</p>
 * 
 * @since 11.0
 */
@Log4j2
@ToString
@RequiredArgsConstructor
public abstract class CategoryDefinition {
    
    @Setter
    private FacetedNavigationProperties facetedNavProps = new FacetedNavigationProperties();

    /**
     * <p>Separator used in PADRE results between a metadata field
     * and its value (Ex: <tt>&lt;rmc item="a:new south wales"&gt;</tt>)</p>
     */
    public static final String MD_VALUE_SEPARATOR = ":";

    /**
     * <p>Separator used in the query string parameters between
     * a facet name and its specific configuration (metadatafield or
     * gscope number).</p>
     * Ex: <tt>f.Location|a=new%20south%20wales</tt>
     */
    public static final String QS_PARAM_SEPARATOR = "|";
    
    /** Name of the facet containing this category type */
    @Getter @Setter protected String facetName;
    
    /**
     * <p>Specific data for this category type.</p>
     * 
     * <p>Depending of the actual type, can be a metadata class,
     * a query expression, etc.</p>
     */
    @NonNull @Getter @Setter protected String data;
    
    /** Label of this category. */
    @Getter @Setter protected String label;
    
    /** List of nested category definitions */
    @Getter protected final List<CategoryDefinition> subCategories = new ArrayList<CategoryDefinition>();
    
    /**
     * Recursively get the list of all query string parameters names used.
     * @return The list of query string parameter names used by this category definition
     * and all its nested definitions.
     */
    public Set<String> getAllQueryStringParamNames() {
        HashSet<String> out = new HashSet<String>();
        out.add(getQueryStringParamName());
        for (CategoryDefinition subDefinition: subCategories) {
            out.addAll(subDefinition.getAllQueryStringParamNames());
        }
        return out;
    }
    
    /**
     * <p>Generates a list of corresponding {@link CategoryValue} by applying
     * this category type rule over a {@link ResultPacket}.</p>
     * 
     * <p>Used to generate categories values on the UI
     * from the faceted navigation configuration.</p>
     * 
     * <p>The size of the list will depend of the type of the category:</p>
     *     <ul>
     *         <li>For "fill" types it will be a multivalued list (metadata field fill, etc.)</li>
     *         <li>For "item" types it will contain a single value (Gscope item, etc.)</li>
     *    </ul>
     * 
     * @param st SearchTransaction to use to compute the values.
     * @return The computed values.
     */
    public List<CategoryValue> computeValues(final SearchTransaction st, FacetDefinition fdef) {
        List<CategoryValue> values = computeData(st, fdef)
            .stream()
            .map(data -> {                
                try {
                    return new CategoryValue(data.getData(), 
                        data.getLabel(), 
                        data.getCount(), 
                        URLEncoder.encode(data.getQueryStringParamName(), "UTF-8")
                            + "=" + URLEncoder.encode(data.getQueryStringParamValue(), "UTF-8"), 
                        data.getConstraint(), 
                        data.isSelected(), 
                        data.getQueryStringParamName(), 
                        data.getQueryStringParamValue()
                        
                        );
                } catch (UnsupportedEncodingException e) {
                    //Will never happen.
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
        if(fdef.getFacetValues() == FacetValues.FROM_SCOPED_QUERY_HIDE_UNSELECTED_PARENT_VALUES
            && !selectedValuesAreNested()
            && values.stream().anyMatch(CategoryValue::isSelected)) {
            return values.stream().filter(CategoryValue::isSelected).collect(Collectors.toList());
        }
        return values;
    }
    
    public abstract List<CategoryValueComputedDataHolder> computeData(final SearchTransaction st, FacetDefinition fdef);
    
    /**
     * Get the query string parameter name for this category.
     * 
     * @return the name of the query string parameter used
     * to select this category (Ex: <tt>f.By Date|dc.date</tt>)
     */
    public final String getQueryStringParamName() {
        return FacetedNavigationUtils.facetParamNamePrefix(getFacetName()) 
            + getQueryStringCategoryExtraPart();
    }
    
    /**
     * Gets the extra part of the query string param name e.g. f.<facet name>|<extra part>=value.
     * 
     * @return
     */
    public abstract String getQueryStringCategoryExtraPart();
    
    /**
     * <p>Given the value of a query string parameter, and any extra parameters,
     * whether this category types is relevant for this parameter.</p>
     * 
     * <p>For example: <tt>f.By Date|dc.date=2010-01-01</tt>:</p>
     * <ul>
     *     <li>value = 2010-01-01</li>
     *     <li>extra = dc.date</li>
     * </ul>
     * <p>A category of type "metadata fill" for the "dc.date" metadata should return
     * true.
     * </p>
     * 
     * 
     * @param value The value to check for.
     * @param extraParams The extra parameter to check for.
     * @return true if this category definition matches, false otherwise.
     */
    public abstract boolean matches(String value, String extraParams);
    
    /**
     * Tests if the facetSelectionDetails matches this category definition.
     * @param facetSelectionDetails
     * @param facetDef The facet definition that this category definition is in.
     * @return
     */
    protected boolean matches(FacetSelectedDetails facetSelectionDetails) {
        if(getFacetName().equals(facetSelectionDetails.getFacetName())) {
            return matches(facetSelectionDetails.getValue(), facetSelectionDetails.getExtraParameter());
        }
        return false;
    }
    
    protected List<FacetSelectedDetails> getMatchingFacetSelectedDetails(SearchQuestion question) {
        return FacetedNavigationUtils.getFacetSelectedDetails(question)
        .stream()
        .filter(facetParam -> matches(facetParam))
        .collect(Collectors.toList());
    }
    
    /**
     * <p>Get additional query processor options to apply for this category definition.</p>
     * 
     * <p>That gives the opportunity to the category definition to add additional QPOs
     * that it may need. QPOs may differ depending if the facet is currently selected or not,
     * such as setting <code>-count_urls</code> dynamically depending on the current number
     * of segments in the URL drill down facet</p>
     * 
     * @param question Can be used to inspect the currently selected facets and return
     *  appropriate QPOs
     * @return A list of query processor options
     */
    public abstract List<QueryProcessorOption<?>> getQueryProcessorOptions(SearchQuestion question);
    
    /**
     * Tells you if all the CategoryValues this CategoryDefiniton can produce are ones that must be 
     * set on the category by the user.
     * 
     * <p>Values defined by the user are ones like gscopes where values not from the user
     * come from other sources such as metadata.</p>
     * 
     * @return true if all values are defined by the user and not generated from the data.
     */
    public abstract boolean allValuesDefinedByUser();
    
    /**
     *
     * @return true if returned selected values are nested.
     */
    public abstract boolean selectedValuesAreNested();
    
    /**
     * <p>Parses a String containing a metadata class and a value
     * such as <tt>x:Red cars</tt>, to separated the metadata class from the value.
     * 
     * @param item Item to parse, containing the metadata class, a colon, and the value
     * @return The metadata + value
     */
    public static MetadataAndValue parseMetadata(String item) {
        if (item == null || item.indexOf(MD_VALUE_SEPARATOR) < 0) {
            return new MetadataAndValue(null, null);
        }
        
        int colon = item.indexOf(MD_VALUE_SEPARATOR);
        return new MetadataAndValue(item.substring(0, colon), item.substring(colon + 1));
    }
    
    protected FacetSearchData getFacetSearchData(SearchTransaction st, FacetDefinition facetDefinition) {
        
        // Usually the response for counts would be the one from the normal search.

        SearchResonseForCountSupplier responseForCounts = (c, v) -> Optional.of(st.getResponse());
        
        SearchResponse searchResponseForValues = st.getResponse();
        
        CountSupplier countIfNotPresent = (c,v) -> null;
        
        // legacy does not work with unscoped values
        if(facetDefinition.getConstraintJoin() != FacetConstraintJoin.LEGACY) {
            
            if(facetDefinition.getFacetValues() == FacetValues.FROM_UNSCOPED_QUERY) {
                searchResponseForValues = Optional.ofNullable(st.getExtraSearches().get(SEARCH_FOR_UNSCOPED_VALUES))
                    .map(SearchTransaction::getResponse)
                    .orElse(searchResponseForValues);
            }
            
            if(facetDefinition.getFacetValues() == FacetValues.FROM_UNSCOPED_ALL_QUERY) {
                searchResponseForValues = Optional.ofNullable(st.getExtraSearches().get(SEARCH_FOR_ALL_VALUES))
                    .map(SearchTransaction::getResponse)
                    .orElse(searchResponseForValues);
            }
            
            if(facetDefinition.getFacetValues() == FacetValues.FROM_UNSCOPED_QUERY 
                || facetDefinition.getFacetValues() == FacetValues.FROM_UNSCOPED_ALL_QUERY
                || facetDefinition.getFacetValues() == FacetValues.FROM_SCOPED_QUERY_WITH_FACET_UNSELECTED) {
                // In the case that the constraints are ANDed then the counts come
                // from the original Response
                // Counts for OR may come from the dedicated extra search if that is the case
                // these values will be overidden later on.
                responseForCounts = (c, v) -> Optional.of(st.getResponse());
                // In the ANDed case any time a value is present but the original Response does not
                // have the value then the count is zero.
                countIfNotPresent = (c,v) -> 0;
            }
            
            if(facetedNavProps.useScopedSearchWithFacetDisabledForValues(facetDefinition, st)) {
                String extraSearchName = new FacetExtraSearchNames().extraSearchWithFacetUnchecked(facetDefinition);
                SearchTransaction extraSearch = st.getExtraSearches().get(extraSearchName);
                if(extraSearch != null && extraSearch.getResponse() != null) {
                    searchResponseForValues = extraSearch.getResponse();
                }
            }
                
            
            if(facetedNavProps.useUnscopedQueryForCounts(facetDefinition, st)) {
                responseForCounts = (c, v) -> Optional.ofNullable(st.getExtraSearches())
                    .map(extraSearches -> extraSearches.get(FacetExtraSearchNames.SEARCH_FOR_UNSCOPED_VALUES))
                    .map(SearchTransaction::getResponse);
                // If the value is value is not present then it must be zero.
                countIfNotPresent = (c,v) -> 0;
                
            }
            
            if(facetedNavProps.useScopedSearchWithFacetDisabledForCounts(facetDefinition, st)) {
                String extraSearch = new FacetExtraSearchNames().extraSearchWithFacetUnchecked(facetDefinition);
                responseForCounts = (c, v) -> Optional.ofNullable(st.getExtraSearches())
                        .map(extraSearches -> extraSearches.get(extraSearch))
                        .map(SearchTransaction::getResponse);
                // if the value is not present in this query then selecting must result in a count of zero.
                countIfNotPresent = (c, v) -> 0;
            }
            
            if(facetedNavProps.useDedicatedExtraSearchForCounts(facetDefinition, st)) {    
                // In the case of OR we might have a extra search that tells us the counts.
                // This is the same for SINGLE_AND_INSELECT_OTHER_FACETS because we need to run a query without something
                // selected to work out the count.
                responseForCounts = (c,v) -> Optional.empty();
                
                countIfNotPresent = (catDef, value) -> {
                    String extraSearchName = new FacetExtraSearchNames()
                            .extraSearchToCalculateCounOfCategoryValue(facetDefinition, catDef, value);
                    log.debug("Using extra search: {} to find the count for category with param name {} and value {}",
                        extraSearchName, catDef.getQueryStringParamName(), value);
                    return Optional.ofNullable(st.getExtraSearches().get(extraSearchName))
                        .map(SearchTransaction::getResponse)
                        .map(r -> r.getResultPacket())
                        .map(r -> r.getResultsSummary())
                        .map(r -> r.getTotalMatching())
                        .orElse(null); // Totall matching?
                };
            }
        }
        
        return new FacetSearchData(searchResponseForValues, responseForCounts, countIfNotPresent);
    }
    
    @AllArgsConstructor
    public static class FacetSearchData {
        @Getter SearchResponse responseForValues;
        
        @Getter SearchResonseForCountSupplier responseForCounts;

        @Getter CountSupplier countIfNotPresent; 
    }
    
    public static interface SearchResonseForCountSupplier extends java.util.function.BiFunction<CategoryDefinition, String, Optional<SearchResponse>> {
        
        /**
         * Given a category definition and a value (e.g. 'David Hawking', '1', etc) this will
         * return the SearchResponse to use to get counts (the number of results returned 
         * if the value is selected). 
         */
        @Override
        Optional<SearchResponse> apply(CategoryDefinition categoryDefinition, String value);
    }
    
    public static interface CountSupplier extends java.util.function.BiFunction<CategoryDefinition, String, Integer> {
        
    }

    /**
     * Aggregate of a metadata class + a value.
     */
    @AllArgsConstructor
    public static class MetadataAndValue {
        
        /** Metadata class, single letter. */
        public String metadataClass;
        
        /** Value. */
        public String value;
    }
    
    
}
