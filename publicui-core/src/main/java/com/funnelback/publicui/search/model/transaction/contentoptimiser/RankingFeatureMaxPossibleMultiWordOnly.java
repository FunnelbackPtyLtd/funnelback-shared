package com.funnelback.publicui.search.model.transaction.contentoptimiser;

import com.funnelback.publicui.search.model.padre.ResultPacket;

public class RankingFeatureMaxPossibleMultiWordOnly extends RankingFeature {

    final boolean maybeMultiword;
    
    public RankingFeatureMaxPossibleMultiWordOnly(String name,String category,ResultPacket rp) {
        super(name,category,rp.getCoolerNames().get(name));
        // IF there are no spaces in the query, it will be a single word query
        maybeMultiword = rp.getQueryCleaned().indexOf(' ') != -1;
    }

    @Override
    public void caculateWin(float selectedScore, float weighting) {
        if(maybeMultiword) {
            win = weighting - selectedScore;
        } else {
            win = 0;
        }
    }


}
