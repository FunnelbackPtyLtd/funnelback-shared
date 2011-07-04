package com.funnelback.contentoptimiser;

import com.funnelback.publicui.search.model.transaction.contentoptimiser.RankingFeature;

public class RankingFeatureMaxPossible extends RankingFeature {

	public RankingFeatureMaxPossible(String name,String category) {
		super(name,category);
	}

	@Override
	public void caculateWin(float selectedScore, float weighting) {
		win = weighting - selectedScore;
	}

}
