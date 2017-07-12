package com.funnelback.publicui.search.model.collection.facetednavigation;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.funnelback.common.facetednavigation.models.FacetConstraintJoin;
import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.publicui.search.model.collection.QueryProcessorOption;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>Category definition for faceted navigation.</p>
 * 
 * <p>A category definition possess a label (Ex: "Cities") and
 * defines the way the values will be generated (Ex: "Take all the
 * existing values of the metadata class X").</p>
 * 
 * @since 11.0
 */
@ToString
@RequiredArgsConstructor
public abstract class CategoryDefinition {

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
        return computeData(st, fdef)
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
    }
    
    public abstract List<CategoryValueComputedDataHolder> computeData(final SearchTransaction st, FacetDefinition fdef);
    
    /**
     * Get the query string parameter name for this category.
     * 
     * @return the name of the query string parameter used
     * to select this category (Ex: <tt>f.By Date|dc.date</tt>)
     */
    public abstract String getQueryStringParamName();
    
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
        Optional<SearchResponse> responseForCounts = Optional.of(st.getResponse());
        
        SearchResponse sr = st.getResponse();
        
        Integer countIfNotPresent = null;
        
        if(facetDefinition.getFacetValues() == FacetValues.FROM_UNSCOPED_QUERY) {
            sr = Optional.ofNullable(st.getExtraSearches().get(SearchTransaction.ExtraSearches.FACETED_NAVIGATION.toString()))
                .map(SearchTransaction::getResponse)
                .orElse(sr);
            
            if(facetDefinition.getConstraintJoin() == FacetConstraintJoin.AND) {
                // In the case that the constraints are ANDed then the constraints come
                // from the original Response 
                responseForCounts = Optional.of(st.getResponse());
                // In the ANDed case any time a value is present but the original Response does not
                // have the value then the count is zero.
                countIfNotPresent = 0;
            }
        }
        
        if(facetDefinition.getConstraintJoin() == FacetConstraintJoin.OR) {
            // In the case of OR our counts are always wrong.
            responseForCounts= Optional.empty();
            countIfNotPresent = null;
        }
        
        return new FacetSearchData(sr, responseForCounts, countIfNotPresent);
    }
    
    @AllArgsConstructor
    public static class FacetSearchData {
        @Getter SearchResponse responseForValues;
        @Getter Optional<SearchResponse> responseForCounts;

        @Getter Integer countIfNotPresent; 
    }

    /**
     * Aggregate of a metadata class + a value.
     */
    @AllArgsConstructor
    public static class MetadataAndValue {
        
        /** Metadata class, single letter. */
        public String metadata;
        
        /** Value. */
        public String value;
    }
    
    
}
