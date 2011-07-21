package com.funnelback.publicui.search.model.transaction.contentoptimiser;

import com.funnelback.publicui.search.model.padre.ResultPacket;


public class RankingFeatureMaxPossible extends RankingFeature {

	public RankingFeatureMaxPossible(String name,String category,ResultPacket rp) {
		super(name,category,rp.getCoolerNames().get(name));
	}

	@Override
	public void caculateWin(float selectedScore, float weighting) {
		win = weighting - selectedScore;
	}

}
