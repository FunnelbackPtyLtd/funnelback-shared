package com.funnelback.contentoptimiser.fetchers;

import com.funnelback.contentoptimiser.UrlStatus;

public interface UrlStatusFetcher {

    UrlStatus fetch(String optimiserUrl, String collection);

}
