package com.funnelback.publicui.search.service.image;

import java.io.IOException;

public interface UrlRenderer {
    byte[] renderUrl(String url, int width, int height) throws IOException;
}

