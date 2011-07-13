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
	@Getter @Setter private DocumentContentModel content = new DocumentContentModel();
	@Getter private final List<String> messages = new ArrayList<String>();
	
	@Getter	private final List<RankingFeature> hintsByWin = new ArrayList<RankingFeature>();
	@Getter	private final Map<String,RankingFeature> hintsByName = new HashMap<String,RankingFeature>();
	
	@Getter private final Map<String, Float> weights = new HashMap<String,Float>();
	
	@Getter private final List<RankingFeatureCategory> hintCollections = new ArrayList<RankingFeatureCategory>();
	@Getter private final Map<String,RankingFeatureCategory> hintCollectionsByName = new HashMap<String,RankingFeatureCategory>();
}
