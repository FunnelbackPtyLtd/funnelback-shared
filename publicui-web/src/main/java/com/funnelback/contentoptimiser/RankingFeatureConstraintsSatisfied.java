package com.funnelback.contentoptimiser;

import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.RankingFeature;

public class RankingFeatureConstraintsSatisfied extends RankingFeature {

    public RankingFeatureConstraintsSatisfied(String name, String category, ResultPacket rp) {
        super(name, category, "Query constraints");
        
    }

    @Override
    public void caculateWin(float selectedScore, float weighting) {
        this.win = 100 * weighting;
    }

}
