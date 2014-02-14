package com.funnelback.publicui.search.service.image;

import java.io.IOException;

/**
 * A bean which is able to scale an image to a specified size.
 */
public interface ImageScaler {

    /**
     * Returns the bytes of the input image (imageData) after being scaled
     * according to settings.
     * 
     * @param scaledCacheIdentifier
     *            A string ID for the given imageData to use as a cache key
     *            (generally expected to be the URL from which the imageData
     *            originated, though this is not specifically required).
     */
    byte[] scaleImage(String scaledCacheIdentifier, byte[] imageData, ImageScalerSettings settings) throws IOException;

    /**
     * Clears any cached information for the given settings, causing the next call to
     * scaleImage to perform the image scaling work again from scratch.
     */
    void clearCache(String scaledCacheIdentifier, ImageScalerSettings ss);

    /**
     * Clear all cached information such that subsequent calls to scaleImage will
     * perform the scaling operation again.
     */
    void clearAllCache();
}

