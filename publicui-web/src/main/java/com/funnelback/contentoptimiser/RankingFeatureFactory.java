package com.funnelback.contentoptimiser;

import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.RankingFeature;

public interface RankingFeatureFactory {

	RankingFeature create(String name, String type,String category, ResultPacket rp);

}
