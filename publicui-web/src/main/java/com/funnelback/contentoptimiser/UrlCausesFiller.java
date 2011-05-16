package com.funnelback.contentoptimiser;

import com.funnelback.publicui.search.model.padre.ResultPacket;

public interface UrlCausesFiller {
	public void fillHints(UrlComparison comparison);

	public void consumeResultPacket(UrlComparison comparison, ResultPacket rp);

	public void setImportantUrl(String url, UrlComparison comparison,
			ResultPacket original);
}
