package com.funnelback.publicui.search.model.collection;

import groovy.lang.Script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.apachecommons.Log;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.paramtransform.TransformRule;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * A search collection.
 */
@ToString
@RequiredArgsConstructor
@JsonIgnoreProperties({"parametersTransforms", "configuration", "quickLinksConfiguration", "hookScriptsClasses"})
@Log
public class Collection {

	/**
	 * Collection types.
	 */
	public static enum Type {
		unknown,web,filecopy,local,database,meta,trim,connector,directory,push;
	}

	/**
	 * Hook scripts
	 */
	public static enum Hook {
		pre_datafetch, post_datafetch;
		
		/**
		 * Name of the Groovy variable that will contain the search transaction
		 */
		public static final String SEARCH_TRANSACTION_KEY = "transaction";
	}
	
	/** Collection id (technical name) */
	@javax.validation.constraints.Pattern(regexp="[\\w-_]+")
	@Getter final private String id;
	
	/** Collection configuration */
	@XStreamOmitField
	@Getter final private Config configuration;
	
	/** Quick Links configuration (quicklinks.cfg) */
	@XStreamOmitField
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
	@XStreamOmitField
	@Getter @Setter private List<TransformRule> parametersTransforms = new ArrayList<TransformRule>();
	
	/**
	 * Custom hook scripts (Groovy)
	 */
	@XStreamOmitField
	@Getter private final Map<Hook, Class<Script>> hookScriptsClasses = new HashMap<Hook, Class<Script>>();
	
	/** Collection type */
	public Type getType() {
		Type out = Type.unknown;
		if (configuration != null && configuration.hasValue(Keys.COLLECTION_TYPE)) {
			try {
				out = Type.valueOf(configuration.value(Keys.COLLECTION_TYPE));
			} catch (IllegalArgumentException iae) {
				log.warn("Unkown collection type: '" + configuration.value(Keys.COLLECTION_TYPE) + "'", iae);
			}
		}
		return out;
	}
	
}
