package com.funnelback.contentoptimiser;

public interface UrlCausesFiller {
	public void FillCauses(UrlComparison comparison);

	public void addUrl(String url, UrlComparison comparison);

	public void setImportantUrl(String url, UrlComparison comparison);
}
