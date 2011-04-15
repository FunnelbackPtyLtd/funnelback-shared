package com.funnelback.publicui.search.model.transaction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
	
	public Facet(String name) {
		this.name = name;
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
		 * Values for this category. Either a single one for
		 * item type {@link CategoryDefinition} or multiple for
		 * fill type {@link CategoryDefinition}.
		 */
		@Getter private final List<CategoryValue> values = new ArrayList<CategoryValue>();
		
		/**
		 * Sub categories, in case of a hierarchical definition
		 */
		@Getter private final List<Category> categories = new ArrayList<Category>();
		
		public Category(String label) {
			this.label = label;
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
