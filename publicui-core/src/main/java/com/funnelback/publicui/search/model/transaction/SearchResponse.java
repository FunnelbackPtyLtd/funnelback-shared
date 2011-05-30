package com.funnelback.publicui.search.model.transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.UrlComparison;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@ToString
@RequiredArgsConstructor
@JsonIgnoreProperties({"rawPacket"})
public class SearchResponse {

	@Getter @Setter private ResultPacket resultPacket;
	
	/** Useful for debugging. */
	@XStreamOmitField
	@Getter @Setter private String rawPacket;
	
	/** PADRE return code (0 = SUCESS) */
	@Getter @Setter private int returnCode;
	
	/** Computed facets */
	@Getter @Setter private List<Facet> facets = new ArrayList<Facet>();
	
	/**
	 * Custom data place holder for custom processors and
	 * hooks. Anything can be put there by users.
	 */
	@Getter private final Map<String, Object> customData = new HashMap<String, Object>();
	
	/**
	 * URL comparison data, for content optimiser.
	 */
	@Getter @Setter private UrlComparison urlComparison;
	
	public boolean hasResultPacket() { return resultPacket != null; }
	
}
