package com.funnelback.publicui.search.model.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.paramtransform.TransformRule;

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
	
	/** Quick Links configuration (quicklinks.cfg) */
	@Getter @Setter private Map<String, String> quickLinksConfiguration;
	
	/** Search profiles (Key = profile id) */
	@Getter private final Map<String, Profile> profiles = new HashMap<String, Profile>();
	
	/** Faceted navigation configuration in conf/faceted_navigation.cfg */
	@Getter @Setter private FacetedNavigationConfig facetedNavigationConfConfig;
	
	/** Faceted navigation configuration in live/idx/faceted_navigation.xml */
	@Getter @Setter private FacetedNavigationConfig facetedNavigationLiveConfig;
	
	/**
	 * In case of a meta collection, list of components collection ids.
	 * Is read from meta.cfg
	 */
	@Getter @Setter private String[] metaComponents = new String[0];
	
	/**
	 * List of parameters transformation (previously known as CGI Transforms).
	 * Is read from cgi_transform.cfg
	 */
	@Getter @Setter private List<TransformRule> parametersTransforms = new ArrayList<TransformRule>();
	
	/** Collection type */
	public Type getType() {
		Type out = Type.unknown;
		if (configuration != null && configuration.hasValue(Keys.COLLECTION_TYPE)) {
			out = Type.valueOf(configuration.value(Keys.COLLECTION_TYPE));
		}
		return out;
	}
	
}
