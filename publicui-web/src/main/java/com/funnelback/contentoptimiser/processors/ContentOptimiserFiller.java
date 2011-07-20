package com.funnelback.contentoptimiser.processors;

import com.funnelback.contentoptimiser.RankingFeatureFactory;
import com.funnelback.publicui.search.model.anchors.AnchorModel;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.ContentOptimiserModel;
import com.google.common.collect.SetMultimap;

public interface ContentOptimiserFiller {
	public void fillHintCollections(ContentOptimiserModel comparison);

	public void consumeResultPacket(ContentOptimiserModel comparison, ResultPacket rp, RankingFeatureFactory hintFactory);
	void setImportantUrl(ContentOptimiserModel comparison, SearchTransaction searchTransaction);


	void obtainContentBreakdown(ContentOptimiserModel comparison,
			SearchTransaction searchTransaction, Result importantResult,
			AnchorModel anchors, SetMultimap<String, String> stemMatches);
}
