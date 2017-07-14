package com.funnelback.publicui.search.model.transaction;
import static com.funnelback.common.function.Predicates.not;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import com.funnelback.common.facetednavigation.models.FacetConstraintJoin;
import com.funnelback.common.facetednavigation.models.FacetSelectionType;
import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.common.function.Flattener;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * <p>Facets, generated from the result data (Metadata counts,
 * GScope counts, URL counts).</p>
 * 
 * @since 11.0
 */
public class Facet {

    /** Facet name, for example "Location" */
    @Getter @Setter private String name;
    
    /** URL to use to unselect all possible values of this facet, built from the current URL
     * 
     * <p>The URL is one where the facet is not selected, the URL may be used as the base URL
     * to append to to select values within the facet.</p>
     *  @Since 15.14
     */
    @Getter @Setter private String unselectAllUrl;
    
    /**
     * Categories definitions of this facet, for example
     * a GScope category, or a Metadata field fill category.
     */
    @Getter private final List<Category> categories = new ArrayList<Category>();
    
    /**
     * Custom data placeholder allowing any arbitrary data to be
     * stored for this facet by hook scripts.
     */
    @Getter private final Map<String, Object> customData = new HashMap<String, Object>();
    
    /**
     * @since 15.14
     */
    @NotNull @NonNull
    @Getter private FacetSelectionType selectionType;
    
    /**
     * @since 15.14
     */
    @NotNull @NonNull
    @Getter private FacetConstraintJoin constraintJoin;
    
    /**
     * @since 15.14
     */
    @NotNull @NonNull
    @Getter private FacetValues facetValues;
    
    public Facet(String name, FacetSelectionType selectionType, FacetConstraintJoin constraintJoin, FacetValues facetValues) {
        this.name = name;
        this.selectionType = selectionType;
        this.constraintJoin = constraintJoin;
        this.facetValues = facetValues;
    }
    
    @Override
    public String toString() {
        return "Facet '" + name + "'";
    }
    
    /**
     * 
     * @return List of selected values, useful to build breadcrumbs
     */
    public List<CategoryValue> getSelectedValues() {
        return getValuesAsStream().filter(CategoryValue::isSelected).collect(Collectors.toList());
    }

    /**
     * 
     * @return List of unselected values
     */
    public List<CategoryValue> getUnselectedValues() {
        return getValuesAsStream().filter(not(CategoryValue::isSelected)).collect(Collectors.toList());
    }
    
    /**
     * 
     * @return List of all facet values both selected and unselected.
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
            .flatMap(Flattener.mapper(Category::getCategories))
            .map(Category::getValues)
            .flatMap(List::stream);
    }

    /**
     * @return True if any of the values of this facet is selected
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
         * <p>Either a single one for item type {@link CategoryDefinition}s
         * or multiple for fill type {@link CategoryDefinition}s.</p>
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
        
        @RequiredArgsConstructor
        public static class ByFirstCategoryValueComparator implements Comparator<Facet.Category> {
            
            @Override
            public int compare(Category c1, Category c2) {
                if (c1.getValues().size() > 0 && c2.getValues().size() > 0) {
                    return Optional.ofNullable(c2.getValues().get(0).getCount()).orElse(Integer.MIN_VALUE) 
                            - Optional.ofNullable(c1.getValues().get(0).getCount()).orElse(Integer.MIN_VALUE);
                } else if (c1.getValues().size() > 0) {
                    return 1;
                } else {
                    return 2;
                }
            }
        }

    }
    
    /**
     * <p>Value of a facet category, such as "Location = Sydney".</p>
     * 
     * <p>Is either automatically generated (fill type {@link CategoryDefinition} or
     * manually created (item type {@link CategoryDefinition}</p>
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
         * @Since 15.14 
         */
        @Getter @Setter private String queryStringParamName;
        
        /** Value of the query string parameter for this value (e.g. <code>Syndey</code>)
         *  
         * @Since 15.14 
         */
        @Getter @Setter private String queryStringParamValue;

        /** URL to use to select this facet, built from the current URL
         *  
         * @Since 15.14 
         */
        @Getter @Setter private String selectUrl;

        /** URL to use to unselect this facet, built from the current URL
         *  
         * @Since 15.14 
         */
        @Getter @Setter private String unselectUrl;
        
        @Getter @Setter private String toggleUrl;

        @Override
        public String toString() {
            return label + " (" + count + "). data=[" + data + "], queryStringParam=[" + queryStringParam + "], selected=" + selected;
        }

        /**
         * Compares category values by number of occurrences
         */
        @RequiredArgsConstructor
        public static class ByCountComparator implements Comparator<Facet.CategoryValue> {
            private final boolean reverse;
            
            @Override
            public int compare(CategoryValue c1, CategoryValue c2) {
                if (reverse) {
                    return c2.getCount() - c1.getCount();
                } else {
                    return c1.getCount() - c2.getCount();
                }
            }
        }
    }
}
