package com.funnelback.publicui.search.service.image;

import java.io.IOException;



public interface ImageFetcher {

    byte[] fetch(String url) throws IOException;

}
