package com.funnelback.contentoptimiser;

import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.UrlComparison;

public interface UrlCausesFiller {
	public void fillHints(UrlComparison comparison);

	public void consumeResultPacket(UrlComparison comparison, ResultPacket rp, HintFactory hintFactory);

	public void setImportantUrl(UrlComparison comparison, ResultPacket resultPacket);
}
