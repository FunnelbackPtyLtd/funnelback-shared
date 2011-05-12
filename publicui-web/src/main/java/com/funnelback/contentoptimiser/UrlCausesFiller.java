package com.funnelback.contentoptimiser;

import com.funnelback.publicui.search.model.padre.ResultPacket;

public interface UrlCausesFiller {
	public void fillHints(UrlComparison comparison);

	public void addUrl(String url, UrlComparison comparison);

	public void setImportantUrl(String url, UrlComparison comparison);

	public void consumeResultPacket(UrlComparison comparison, ResultPacket rp);
}
