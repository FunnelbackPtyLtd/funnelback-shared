package com.funnelback.publicui.search.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;

/**
 * A search collection.
 */
@ToString
@RequiredArgsConstructor
public class Collection {

	public static enum Type {
		unknown,web,filecopy,local,database,meta,trim,connector;
	}
	
	/** Collection id (technical name) */
	@Getter final private String id;
	
	/** Collection configuration */
	@Getter final private Config configuration;
	
	/** Faceted navigation configuration (faceted_navigation.cfg) */
	@Getter @Setter private FacetedNavigationConfig facetedNavigationConfig;
	
	/**
	 * In case of a meta collection, list of components collection ids.
	 * Is read from meta.cfg
	 */
	@Getter @Setter private String[] metaComponents = new String[0];
	
	/** Collection type */
	public Type getType() {
		Type out = Type.unknown;
		if (configuration != null && configuration.hasValue(Keys.COLLECTION_TYPE)) {
			out = Type.valueOf(configuration.value(Keys.COLLECTION_TYPE));
		}
		return out;
	}
	
	/* TODO
	private ... cgiTransformConfig;
	private ... contextualNavigationConfig;
	private ... synonymsConfig;
	...
	*/
}
