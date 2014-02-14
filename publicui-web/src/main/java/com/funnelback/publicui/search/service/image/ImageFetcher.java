package com.funnelback.publicui.search.service.image;

import java.io.IOException;

/**
 * A bean which is able to fetch (and possibly cache) content form a url.
 */
public interface ImageFetcher {

    /**
     * Sends a request to url and returns a byte array of the content. Content
     * from the url may be cached at the implementations discretion to avoid the
     * need to perform network requests on subsequent requests with the same
     * URL.
     * 
     * @throws IOException
     *             If an error occurs during fetching
     */
    byte[] fetch(String url) throws IOException;

    /**
     * Clears any cached content for the given url, ensuring the next call to
     * fetch will load data from the source.
     */
    void clearCache(String url);

    /**
     * Clears any cached content for all urls, ensuring that subsequent calles
     * to fetch will re-load data for each new url.
     */
    void clearAllCache();

}
