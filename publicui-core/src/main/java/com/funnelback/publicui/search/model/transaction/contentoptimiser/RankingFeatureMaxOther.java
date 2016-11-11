package com.funnelback.publicui.search.model.transaction.contentoptimiser;

import com.funnelback.publicui.search.model.padre.ResultPacket;



public class RankingFeatureMaxOther extends RankingFeature {

    public RankingFeatureMaxOther(String name,String category,ResultPacket rp) {
        super(name,category,
            rp.getCoolerNames().entrySet()
            .stream()
            .filter(e -> e.getKey().getName().equals(name))
            .map(e -> e.getValue())
            .findFirst()
            .orElse(null));
    }

    @Override
    public void caculateWin(float selectedScore, float weighting) {
        win = maxScore - selectedScore;
    }

}
