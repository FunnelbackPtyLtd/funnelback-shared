package com.funnelback.publicui.search.model.padre;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Contextual navigation category (type, topic, site, ...)
 */
@RequiredArgsConstructor
public class Category {

	@Getter final private String name;
	@Getter final private Integer more;
	
	@Getter @Setter private String moreLink;
	@Getter @Setter private String fewerLink;
	
	@Getter private final List<Cluster> clusters = new ArrayList<Cluster>();
	
	public final static class Schema {
		public static final String CATEGORY = "category";
		
		public static final String NAME = "name";
		public static final String MORE = "more";
		
		public static final String MORE_LINK = "more_link";
		public static final String FEWER_LINK = "fewer_link";
	}
}
