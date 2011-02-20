package com.funnelback.publicui.search.model.transaction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public class Facet {

	@Getter @Setter private String name;
	@Getter private final List<CategoryType> categoryTypes = new ArrayList<CategoryType>();
	
	public Facet(String name) {
		this.name = name;
	}
	
	public static class CategoryType {
		@Getter @Setter private String label;
		@Getter private final List<Category> categories = new ArrayList<Category>();
		@Getter private final List<CategoryType> subCategoryTypes = new ArrayList<CategoryType>();
		
		public CategoryType(String label) {
			this.label = label;
		}
	}
	
	@AllArgsConstructor
	public static class Category {
		@Getter @Setter private String data;
		@Getter @Setter private String label;
		@Getter @Setter private int count;
		@Getter @Setter private String urlParams;
		
		@RequiredArgsConstructor
		public static class ByCountComparator implements Comparator<Facet.Category> {
			private final boolean reverse;
			
			@Override
			public int compare(Category c1, Category c2) {
				if (reverse) {
					return c2.getCount() - c1.getCount();
				} else {
					return c1.getCount() - c2.getCount();
				}
			}
		}
	}
	
}
