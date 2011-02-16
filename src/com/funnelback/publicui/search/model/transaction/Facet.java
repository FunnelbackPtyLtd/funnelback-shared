package com.funnelback.publicui.search.model.transaction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Facet {

	@Getter private final String name;
	@Getter private final List<Category> categories = new ArrayList<Category>();
	
	@RequiredArgsConstructor
	public static class Category {
		@Getter private final String data;
		@Getter private final String label;
		@Getter private final int count;
		@Getter private final String urlParams;
		@Getter private final List<Category> categories = new ArrayList<Category>();
		
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
