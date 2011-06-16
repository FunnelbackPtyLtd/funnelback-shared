package com.funnelback.contentoptimiser;

import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.UrlComparison;

public interface UrlCausesFiller {
	public void fillHintCollections(UrlComparison comparison);

	public void consumeResultPacket(UrlComparison comparison, ResultPacket rp, HintFactory hintFactory);
	void setImportantUrl(UrlComparison comparison, SearchTransaction searchTransaction);

	void obtainContentBreakdown(UrlComparison comparison,
			SearchTransaction searchTransaction, ResultPacket importantRp);
}
