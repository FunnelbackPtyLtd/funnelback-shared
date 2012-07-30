package com.funnelback.publicui.search.service.image;

import java.io.IOException;



public interface ImageScaler {

	byte[] scaleImage(String scaledCacheIdentifier, byte[] imageData, ImageScalerSettings settings) throws IOException;
}

