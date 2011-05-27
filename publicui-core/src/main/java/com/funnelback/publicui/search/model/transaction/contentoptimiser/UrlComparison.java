package com.funnelback.publicui.search.model.transaction.contentoptimiser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

public class UrlComparison {
	
	@Getter private final List<UrlInfoAndScore> urls = new ArrayList<UrlInfoAndScore>();
	
	@Getter @Setter private UrlInfoAndScore importantOne;
	
	@Getter	private final List<Hint> hintsByWin = new ArrayList<Hint>();
	@Getter	private final Map<String,Hint> hintsByName = new HashMap<String,Hint>();
	
	@Getter private final Map<String, Float> weights = new HashMap<String,Float>();

}
