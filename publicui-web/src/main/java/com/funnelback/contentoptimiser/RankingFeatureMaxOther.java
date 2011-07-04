package com.funnelback.contentoptimiser;

import com.funnelback.publicui.search.model.transaction.contentoptimiser.RankingFeature;


public class RankingFeatureMaxOther extends RankingFeature {

	public RankingFeatureMaxOther(String name,String category) {
		super(name,category);
	}

	@Override
	public void caculateWin(float selectedScore, float weighting) {
		win = maxScore - selectedScore;
	}

}
