package com.funnelback.publicui.search.model.collection.facetednavigation;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public abstract class Category {

	@Getter @Setter private String data;
	@Getter private final List<Category> subCategories = new ArrayList<Category>();
	
}
