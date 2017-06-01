package com.funnelback.publicui.search.model.transaction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;

/**
 * <p>Facets, generated from the result data (Metadata counts,
 * GScope counts, URL counts).</p>
 * 
 * @since 11.0
 */
public class Facet {

    /** Facet name, for example "Location" */
    @Getter @Setter private String name;
    
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
    
    public Facet(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return "Facet '" + name + "'";
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
                    return c2.getValues().get(0).getCount() - c1.getValues().get(0).getCount();
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
        
        /** Actual value of the category (Ex: "Sydney"). */
        @Getter @Setter private String data;
        
        /** Label of the value, usually the same as the data. */
        @Getter @Setter private String label;
        
        /** Count of occurrences for this value */
        @Getter @Setter private int count;
        
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
