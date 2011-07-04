package com.funnelback.contentoptimiser;

import com.funnelback.publicui.search.model.anchors.AnchorModel;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.ContentOptimiserModel;

public interface UrlCausesFiller {
	public void fillHintCollections(ContentOptimiserModel comparison);

	public void consumeResultPacket(ContentOptimiserModel comparison, ResultPacket rp, RankingFeatureFactory hintFactory);
	void setImportantUrl(ContentOptimiserModel comparison, SearchTransaction searchTransaction);

	void obtainContentBreakdown(ContentOptimiserModel comparison,
			SearchTransaction searchTransaction, ResultPacket importantRp, AnchorModel anchors);
}
