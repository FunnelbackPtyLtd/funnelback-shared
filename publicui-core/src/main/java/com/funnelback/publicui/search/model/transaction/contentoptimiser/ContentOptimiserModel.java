package com.funnelback.publicui.search.model.transaction.contentoptimiser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.funnelback.publicui.search.model.padre.Result;

import lombok.Getter;
import lombok.Setter;

public class ContentOptimiserModel {
	
	@Getter private final List<Result> urls = new ArrayList<Result>();
	
	@Getter @Setter private Result importantOne;
	@Getter private final List<String> messages = new ArrayList<String>();
	
	@Getter	private final List<Hint> hintsByWin = new ArrayList<Hint>();
	@Getter	private final Map<String,Hint> hintsByName = new HashMap<String,Hint>();
	
	@Getter private final Map<String, Float> weights = new HashMap<String,Float>();
	
	@Getter private final List<HintCollection> hintCollections = new ArrayList<HintCollection>();
	@Getter private final Map<String,HintCollection> hintCollectionsByName = new HashMap<String,HintCollection>();
}
