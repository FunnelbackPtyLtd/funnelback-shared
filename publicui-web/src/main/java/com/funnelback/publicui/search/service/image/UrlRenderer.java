package com.funnelback.publicui.search.service.image;

import java.io.IOException;

/**
 * Renders URL (screenshots)
 *
 */
public interface UrlRenderer {
    
    /**
     * Renders a URL
     * @param url URL to render
     * @param width Desired width
     * @param height Desired height
     * @return The rendered URL in an image binary form
     * @throws IOException If something goes wrong
     */
    byte[] renderUrl(String url, int width, int height) throws IOException;
}

