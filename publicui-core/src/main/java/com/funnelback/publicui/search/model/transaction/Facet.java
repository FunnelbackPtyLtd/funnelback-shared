package com.funnelback.publicui.search.model.transaction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Facet, coming from results.
 * 
 * For instance: "Location"
 */
public class Facet {

	/** Facet name */
	@Getter @Setter private String name;
	
	/** Category of this facet */
	@Getter private final List<Category> categories = new ArrayList<Category>();
	
	/**
	 * Custom data place holder for custom processors and
	 * hooks. Anything can be put there by users.
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
	 * Recursively check if this facet has any value at all
	 * @return
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
	 * Category of a facet.
	 * Correspond to the <i>definition</i> of a category,
	 * not the value itself.
	 */
	public static class Category {
		
		/**
		 * Label for this category
		 */
		@Getter @Setter private String label;
		
		/**
		 * Name of the query string parameter for this category
		 */
		@Getter @Setter private String queryStringParamName;
		
		/**
		 * Values for this category. Either a single one for
		 * item type {@link CategoryDefinition} or multiple for
		 * fill type {@link CategoryDefinition}.
		 */
		@Getter private final List<CategoryValue> values = new ArrayList<CategoryValue>();
		
		/**
		 * Sub categories, in case of a hierarchical definition
		 */
		@Getter private final List<Category> categories = new ArrayList<Category>();
		
		public Category(String label, String queryStringParamName) {
			this.label = label;
			this.queryStringParamName = queryStringParamName;
		}
		
		/**
		 * Recursively check if this category or any of its sub-categories
		 * has values.
		 * @return
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
		
		@Override
		public String toString() {
			return "Category '" + label + "' (" + values.size() + " values, " + categories.size() + " sub-categories)";
		}
	}
	
	/**
	 * Value of a category.
	 * 
	 * Is either automatically generated (fill type {@link CategoryDefinition} or
	 * manuall (item type {@link CategoryDefinition}
	 */
	@AllArgsConstructor
	public static class CategoryValue {
		
		@Getter @Setter private String data;
		
		/** Label of the value */
		@Getter @Setter private String label;
		
		/** Count of occurences for this value */
		@Getter @Setter private int count;
		
		/** Query String parameters to use to select this value */
		@Getter @Setter private String queryStringParam;

		@Override
		public String toString() {
			return label + " (" + count + "). data=[" + data + "], queryStringParam=[" + queryStringParam + "]";
		}

		/**
		 * Compares by number of occurences
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
