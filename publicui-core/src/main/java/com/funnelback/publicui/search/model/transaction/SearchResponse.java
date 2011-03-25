package com.funnelback.publicui.search.model.transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.publicui.search.model.padre.ResultPacket;

@ToString
@RequiredArgsConstructor
public class SearchResponse {

	@Getter @Setter private ResultPacket resultPacket;
	
	/** Useful for debugging. We should remove it for production */
	@Getter @Setter private String rawPacket;
	
	/** PADRE return code (0 = SUCESS) */
	@Getter @Setter private int returnCode;
	
	/** Computed facets */
	@Getter @Setter private List<Facet> facets = new ArrayList<Facet>();
	
	/**
	 * Custom data place holder for custom processors and
	 * hooks. Anything can be put there by users.
	 */
	@Getter private Map<String, Object> customData = new HashMap<String, Object>();
	
	public boolean hasResultPacket() { return resultPacket != null; }
	
}
