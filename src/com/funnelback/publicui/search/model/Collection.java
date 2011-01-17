package com.funnelback.publicui.search.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;

@ToString
@RequiredArgsConstructor
public class Collection {

	public static enum Type {
		unknown,web,filecopy,local,database,meta,trim,connector;
	}
	
	@Getter final private String id;
	@Getter final private Config configuration;
	
	@Getter @Setter private FacetedNavigationConfig facetedNavigationConfig;
	
	public Type getType() {
		Type out = Type.unknown;
		if (configuration != null && configuration.hasValue(Keys.COLLECTION_TYPE)) {
			out = Type.valueOf(configuration.value(Keys.COLLECTION_TYPE));
		}
		return out;
	}
	
	/* TODO
	private ... facetedNavigationConfig;
	private ... cgiTransformConfig;
	private ... contextualNavigationConfig;
	private ... synonymsConfig;
	...
	*/
}
