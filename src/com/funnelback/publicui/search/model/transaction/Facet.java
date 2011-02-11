package com.funnelback.publicui.search.model.transaction;

import java.util.ArrayList;
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
	}
	
}
