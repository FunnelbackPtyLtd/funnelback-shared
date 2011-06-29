package com.funnelback.publicui.search.model.anchors;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class AnchorDetail {

	public static final int MAX_URLS_PER_PAGE = 50;
	
	@Getter private final List<String> urls = new ArrayList<String>();
	@Getter private final String linkAnchortext;
	@Getter @Setter private int size;
	@Getter @Setter private int start;
	@Getter @Setter private int end;
	
	
}
