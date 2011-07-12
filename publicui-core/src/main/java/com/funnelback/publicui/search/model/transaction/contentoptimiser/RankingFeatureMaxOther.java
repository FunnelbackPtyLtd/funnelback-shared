package com.funnelback.publicui.search.model.transaction.contentoptimiser;



public class RankingFeatureMaxOther extends RankingFeature {

	public RankingFeatureMaxOther(String name,String category) {
		super(name,category);
	}

	@Override
	public void caculateWin(float selectedScore, float weighting) {
		win = maxScore - selectedScore;
	}

}
