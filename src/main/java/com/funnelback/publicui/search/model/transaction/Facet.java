package com.funnelback.publicui.search.model.transaction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.funnelback.common.facetednavigation.models.FacetConstraintJoin;
import com.funnelback.common.facetednavigation.models.FacetSelectionType;
import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.common.facetednavigation.models.FacetValuesOrder;
import com.funnelback.publicui.search.model.transaction.facet.FacetDisplayType;
import com.funnelback.publicui.search.model.transaction.facet.order.FacetComparatorProvider;
import com.funnelback.publicui.xml.FacetConverter;
import com.google.common.base.Function;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
     * Categories definitions of this facet, for example
     * a GScope category, or a Metadata field fill category.
     * 
     * <p>This should not be used when displaying facets. Instead use
     *  allValues and selectedValues.</p> 
     */
    @Getter private final List<Category> categories = new ArrayList<Category>();
    
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
        return getValuesAsStream().filter(CategoryValue::isSelected).collect(Collectors.toList());
    }
    
    /**
     * 
     * @return List of all facet values both selected and unselected.
     * @Since 15.12
     */
    public List<Facet.CategoryValue> getAllValues() {
        return getValuesAsStream().collect(Collectors.toList());
    }
    
    /**
     * Recursively check if this facet has any value at all.
     * @return true if the facet possess at least one value.
     */
    public boolean hasValues() {
        for (Category category: categories) {
            if (category.hasValues() ) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Recursively finds the deepest category.
     * @param categoryParamNames
     * @return The deepest {@link Category} matching the parameter names.
     */
    public Category findDeepestCategory(List<String> categoryParamNames) {
        for (Category category: categories) {
            Category deepest = category.findDeepest(categoryParamNames);
            if (deepest != null) {
                return deepest;
            }
        }
        return null;
    }
    
    private Stream<Facet.CategoryValue> getValuesAsStream() {
         return this.getCategories().stream()
            .flatMap(mapper(Category::getCategories))
            .map(Category::getValues)
            .flatMap(List::stream)
            .sorted(comparatorForSorting());
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
     * Clone from com.funnelback.common.function.Flattener.flatten(T, Function<T, Collection<T>>)
     * 
     * @param <T>
     * @param value
     * @param getChildren
     * @return
     */
    private static <T>  Stream<T> flatten(T value, Function<T, Collection<T>> getChildren) {
        // This is a more generic form of:
        // https://stackoverflow.com/questions/32656888/recursive-use-of-stream-flatmap
        return Stream.concat(ofNullableSingle(value), 
            Optional.ofNullable(value).map(getChildren::apply).orElse(Collections.emptyList()).stream()
                .flatMap(child -> flatten(child, getChildren)));
    }
    
    /**
     * Clone from com.funnelback.common.function.Flattener.mapper(Function<T, Collection<T>>)
     * 
     * @param <T>
     * @param getChildren
     * @return
     */
    private static <T> Function<T, Stream<T>> mapper(Function<T, Collection<T>> getChildren) {
        return (value) -> flatten(value, getChildren);
    }
    
    @JsonIgnore
    private final Comparator<CategoryValue> comparatorForSorting() {
        return new FacetComparatorProvider()
            .getComparatorWhenSortingAllValus(order, Optional.ofNullable(customComparator));
    }

    /**
     * @return True if any of the values of this facet is selected
     * @Since 15.12
     */
    public boolean isSelected() {
        return getValuesAsStream().anyMatch(CategoryValue::isSelected);
    }
    
    /**
     * <p>Category of a facet, such as "Location, based on the metadata class X".</p>
     * 
     * <p>Correspond to the <i>definition</i> of a category,
     * not the value itself.</p>
     */
    public static class Category {
        
        /**
         * Label for this category.
         */
        @Getter @Setter private String label;
        
        /**
         * Name of the query string parameter for this category.
         * (Ex: <code>f.Location|X).</code>
         */
        @Getter @Setter private String queryStringParamName;
        
        /**
         * <p>Values for this category.</p>
         * 
         */
        @Getter private final List<CategoryValue> values = new ArrayList<CategoryValue>();
        
        /**
         * Sub categories, in case of a hierarchical definition.
         */
        @Getter private final List<Category> categories = new ArrayList<Category>();
        
        public Category(String label, String queryStringParamName) {
            this.label = label;
            this.queryStringParamName = queryStringParamName;
        }
        
        /**
         * Recursively check if this category or any of its sub-categories
         * has values.
         * @return true if this category or a nested one has at least one value.
         */
        public boolean hasValues() {
            if (values.size() > 0) {
                return true;
            } else {
                for (Category subCategory: categories) {
                    if (subCategory.hasValues()) {
                        return true;
                    }
                }
                return false;
            }
        }
        
        /**
         * Recursively find the deepest category.
         * @param categoryParamNames
         * @return @return The deepest {@link Category} matching the parameter names.
         */
        public Category findDeepest(List<String> categoryParamNames) {
            Category out = null;
            if (categoryParamNames.contains(this.queryStringParamName)) {
                out = this;
            }
            
            for (Category c: categories) {
                Category deepest = c.findDeepest(categoryParamNames);
                if (deepest != null) {
                    out = deepest;
                    break;
                }
            }
            return out;
        }
        
        @Override
        public String toString() {
            return "Category '" + label + "' (" + values.size() + " values, " + categories.size() + " sub-categories)";
        }

    }
    
    /**
     * <p>Value of a facet category, such as "Location = Sydney".</p>
     * 
     */
    @AllArgsConstructor
    public static class CategoryValue {
        
        /**
         * Backwards compatible constructor where not all fields are set, may result in problems.
         * 
         * @param data
         * @param label
         * @param count
         * @param queryStringParam
         * @param constraint
         * @param selected
         */
        @Deprecated
        public CategoryValue(String data, String label, Integer count, String queryStringParam, String constraint,
            boolean selected) {
            super();
            this.data = data;
            this.label = label;
            this.count = count;
            this.queryStringParam = queryStringParam;
            this.constraint = constraint;
            this.selected = selected;
        }
        
        
        public CategoryValue(String data, String label, Integer count, 
            String queryStringParam, String constraint, boolean selected,
            String queryStringParamName, String queryStringParamValue) {
          super();
          this.data = data;
          this.label = label;
          this.count = count;
          this.queryStringParam = queryStringParam;
          this.constraint = constraint;
          this.selected = selected;
          this.queryStringParamName = queryStringParamName;
          this.queryStringParamValue = queryStringParamValue;
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
         * The depth of the category definition, used for sorting.
         * 
         * @since 15.12
         */
        @XStreamOmitField
        @JsonIgnore
        @Getter @Setter private int categoryDepth =  Integer.MAX_VALUE;

        @Override
        public String toString() {
            return label + " (" + count + "). data=[" + data + "], queryStringParam=[" + queryStringParam + "], selected=" + selected;
        }
    }
}
