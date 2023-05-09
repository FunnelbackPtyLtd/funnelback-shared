package com.funnelback.plugin.starturl;

import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * Provide extra start URLs to use during crawling
 */
public interface StartUrlProvider {

    default List<URL> extraStartUrls(StartUrlProviderContext context) {
        return Collections.emptyList();
    }

}
