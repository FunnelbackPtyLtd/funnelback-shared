package com.funnelback.publicui.search.model.transaction;

import java.util.ArrayList;
import java.util.List;

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
	
	@Getter @Setter private List<Facet> facets = new ArrayList<Facet>();
	
	public boolean hasResultPacket() { return resultPacket != null; }
	
}
