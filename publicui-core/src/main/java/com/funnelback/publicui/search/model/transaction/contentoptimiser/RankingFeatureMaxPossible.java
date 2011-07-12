package com.funnelback.publicui.search.model.transaction.contentoptimiser;


public class RankingFeatureMaxPossible extends RankingFeature {

	public RankingFeatureMaxPossible(String name,String category) {
		super(name,category);
	}

	@Override
	public void caculateWin(float selectedScore, float weighting) {
		win = weighting - selectedScore;
	}

}
