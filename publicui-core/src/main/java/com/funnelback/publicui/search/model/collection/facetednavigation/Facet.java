package com.funnelback.publicui.search.model.collection.facetednavigation;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public class Facet {

	@Getter private final String name;
	@Getter private final List<CategoryType> categoryTypes;
	
	public final class Schema {
		public static final String FACET = "Facet";
		public static final String DATA = "Data";
	}
}
