package com.funnelback.publicui.search.model.transaction;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.funnelback.common.facetednavigation.models.FacetConstraintJoin;
import com.funnelback.common.facetednavigation.models.FacetSelectionType;
import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.common.facetednavigation.models.FacetValuesOrder;
import com.funnelback.publicui.search.model.transaction.facet.FacetDisplayType;
import com.funnelback.publicui.xml.FacetConverter;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
/**
 * <p>Facets, generated from the result data (Metadata counts,
 * GScope counts, URL counts).</p>
 * 
 * @since 11.0
 */
@XStreamConverter(FacetConverter.class)
@AllArgsConstructor(access=AccessLevel.PROTECTED)
public class Facet {

    /** Facet name, for example "Location" */
    @Getter @Setter private String name;
    
    /** URL to use to unselect all possible values of this facet, built from the current URL
     * 
     * <p>The URL is one where the facet is not selected, the URL may be used as the base URL
     * to append to to select values within the facet.</p>
     *  @Since 15.12
     */
    @Getter @Setter private String unselectAllUrl;
    
    /**
     * Custom data placeholder allowing any arbitrary data to be
     * stored for this facet by hook scripts.
     */
    @Getter private final Map<String, Object> customData = new HashMap<String, Object>();
    
    /**
     * @since 15.12
     */
    @NotNull @NonNull
    @Getter private FacetSelectionType selectionType;
    
    /**
     * @since 15.12
     */
    @NotNull @NonNull
    @Getter private FacetConstraintJoin constraintJoin;
    
    /**
     * @since 15.12
     */
    @NotNull @NonNull
    @Getter @Setter private List<FacetValuesOrder> order;
    
    /**
     * @since 15.12
     */
    @NotNull @NonNull
    @Getter private FacetValues facetValues;
    
    /**
     * The guessed type of the facet based on its configuration.
     * 
     * <p>This value can give an indication of how the facet
     * should be displayed, for example SINGLE select where values
     * come from a UNSCOPED query would give the value RADIO_BUTTON.
     * The UI could use this to show the facets as radio buttons.</p>
     * 
     * <p>If the guessedType is not the way you intend to show the facet
     * you should ignore this value and instead use the facet name or
     * other fields which describe the facet to work out what should be 
     * shown i.e. don't have code which is if(guessedType == TAB) then
     * show the facet as checkboxes.</p>
     * 
     * <p>Newer versions of Funnelback may change how the type is guessed.</p>
     * 
     * @since 15.12
     */
    @NotNull @NonNull
    @Getter private FacetDisplayType guessedDisplayType;
    
    /**
     * If non-null this comparator will be used to sort values returned by the 
     * {@link Facet#getAllValues()}, {@link Facet#getSelectedValues()} and 
     * {@link Facet#getUnselectedValues()} methods.
     * 
     * <p>This currently does not sort the values returned by any other method
     * such as the values in what is returned by {@link Facet#getCategories()}
     * although this may change.</p>
     * 
     * @since 15.12
     */
    @JsonIgnore @XStreamOmitField
    @Getter @Setter private Comparator<CategoryValue> customComparator = null;
    
    /**
     * 
     * @return List of all facet values both selected and unselected.
     * @Since 15.12
     */
    @Getter private final List<Facet.CategoryValue> allValues = new ArrayList<>();
    
    public Facet(String name, 
            FacetSelectionType selectionType, 
            FacetConstraintJoin constraintJoin, 
            FacetValues facetValues,
            List<FacetValuesOrder> order) {
        this.name = name;
        this.selectionType = selectionType;
        this.constraintJoin = constraintJoin;
        this.facetValues = facetValues;
        this.order = order;
        this.guessedDisplayType = FacetDisplayType.getType(selectionType, constraintJoin, facetValues);
    }
    
    /**
     * Backwards compatible constructor so the stencils facet will continue to work.
     * 
     * @param name
     */
    @Deprecated
    public Facet(String name) {
        this(name, 
            FacetSelectionType.SINGLE,
            FacetConstraintJoin.LEGACY,
            FacetValues.FROM_SCOPED_QUERY,
            List.of(FacetValuesOrder.SELECTED_FIRST, FacetValuesOrder.COUNT_DESCENDING));
    }
    
    @Override
    public String toString() {
        return "Facet '" + name + "'";
    }
    
    /**
     * 
     * @return List of selected values, useful to build breadcrumbs
     * @Since 15.12
     */
    public List<CategoryValue> getSelectedValues() {
        return allValues.stream().filter(CategoryValue::isSelected).collect(Collectors.toList());
    }
    
    /**
     * Recursively check if this facet has any value at all.
     * @return true if the facet possess at least one value.
     */
    public boolean hasValues() {
        return !allValues.isEmpty();
    }
    
    /**
     * Clone from com.funnelback.common.function.StreamUtils.ofNullableSingle(T)
     * 
     * @param <T>
     * @param a
     * @return
     */
    public static <T> Stream<T> ofNullableSingle(T a) {
        if(a == null) return Stream.empty();
        return Stream.of(a);
    }

    /**
     * @return True if any of the values of this facet is selected
     * @Since 15.12
     */
    public boolean isSelected() {
        return getAllValues().stream().anyMatch(CategoryValue::isSelected);
    }
    
    /**
     * <p>Value of a facet category, such as "Location = Sydney".</p>
     * 
     */
    @AllArgsConstructor
    public static class CategoryValue {
        
        @Builder
        public CategoryValue(String data, String label, Integer count, 
            String queryStringParam, String constraint, boolean selected,
            String queryStringParamName, String queryStringParamValue,
            int categoryDefinitionIndex) {
          super();
          this.data = data;
          this.label = label;
          this.count = count;
          this.queryStringParam = queryStringParam;
          this.constraint = constraint;
          this.selected = selected;
          this.queryStringParamName = queryStringParamName;
          this.queryStringParamValue = queryStringParamValue;
          this.categoryDefinitionIndex = categoryDefinitionIndex;
      }

        /** Actual value of the category (Ex: "Sydney"). */
        @Getter @Setter private String data;
        
        /** Label of the value, usually the same as the data. */
        @Getter @Setter private String label;
        
        /** Count of occurrences for this value */
        @Getter @Setter private Integer count;
        
        /**
         * Query String parameters to use to select this value
         * (Ex: <code>f.Location|X=Sydney</code>).
         **/
        @Getter @Setter private String queryStringParam;
        
        /**
         * Constraint used to get this value. Can be a metadata class
         * or a GScope number, depending of the facet type.
         * 
         * @since 11.2
         */
        @Getter @Setter private String constraint;
        
        /**
         * Indicates if this value is currently selected
         * 
         * @since 15.8
         */
        @Getter @Setter private boolean selected;
        
        /** Name of the query string parameter for this value (e.g. <code>f.Location|X</code>)
         * 
         * @Since 15.12 
         */
        @Getter @Setter private String queryStringParamName;
        
        /** Value of the query string parameter for this value (e.g. <code>Syndey</code>)
         *  
         * @Since 15.12 
         */
        @Getter @Setter private String queryStringParamValue;
        
        /** URL to use to toggle the select status of the facet value.
         *  
         * @Since 15.12 
         */
        @Getter @Setter private String toggleUrl;
        
        /**
         * The depth of the category value, used for sorting.
         * 
         * @since 16.0
         */
        @XStreamOmitField
        @JsonIgnore
        @Getter @Setter private int categoryValueDepth =  Integer.MAX_VALUE;
        
        /**
         * The category definition index, used for sorting by category definition.
         * 
         * // TODO sort on this. 
         * @since 16.0
         */
        @XStreamOmitField
        @JsonIgnore
        @Getter private final int categoryDefinitionIndex;

        @Override
        public String toString() {
            return label + " (" + count + "). data=[" + data + "], queryStringParam=[" + queryStringParam + "], selected=" + selected;
        }
    }
}
