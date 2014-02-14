package com.funnelback.publicui.search.service.image;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
import net.coobird.thumbnailator.geometry.Positions;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.service.image.ImageScalerSettings.ScaleType;

@Log4j
@Component
public class DefaultImageScaler implements ImageScaler {
    
    protected static final String CACHE = DefaultImageScaler.class.getSimpleName() + "Repository";

    @Autowired
    protected CacheManager appCacheManager;

    @Override
    public byte[] scaleImage(String scaledCacheIdentifier, byte[] imageData, ImageScalerSettings settings) throws IOException {

        Cache cache = appCacheManager.getCache(CACHE);

        String key = getCacheKey(scaledCacheIdentifier, settings);

        if (! cache.isKeyInCache(key)) {
            log.trace("Scaling " + scaledCacheIdentifier + " to cache with key " + key);
            
            @Cleanup ByteArrayInputStream unscaledImageInputStream = new ByteArrayInputStream(imageData);
            Builder<? extends InputStream> thumbnailor = Thumbnails.of(unscaledImageInputStream);
            
            thumbnailor = thumbnailor.size(settings.getWidth(), settings.getHeight());
    
            if (settings.getFormat() != null) {
                thumbnailor = thumbnailor.outputFormat(settings.getFormat());
            }        
    
            if (settings.getType() == null) {
                thumbnailor = thumbnailor.keepAspectRatio(true);
            } else if (settings.getType() == ScaleType.keep_aspect) {
                thumbnailor = thumbnailor.keepAspectRatio(true);
            } else if (settings.getType() == ScaleType.ignore_aspect) {
                thumbnailor = thumbnailor.keepAspectRatio(false);
            } else {
                String cropName = settings.getType().name();
                // Strip prefix to match position names
                String positionName = cropName.substring("crop_".length());
                thumbnailor = thumbnailor.crop(Positions.valueOf(positionName.toUpperCase()));
            }
    
            @Cleanup ByteArrayOutputStream scaledImageBytes = new ByteArrayOutputStream();
            try {
                thumbnailor.toOutputStream(scaledImageBytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            
            cache.put(new Element(key, scaledImageBytes.toByteArray()));
        }

        return (byte[])cache.get(key).getValue();
    }

    /**
     * Returns the cache key, which should be distinct for distinct input values.
     */
    private String getCacheKey(String scaledCacheIdentifier, ImageScalerSettings settings) {
        String processedKey = scaledCacheIdentifier + " " + settings.getWidth()
                + "|" + settings.getHeight() + "|"
                + settings.getType() + "|" + settings.getFormat();
        return processedKey;
    }

    @Override
    public void clearCache(String scaledCacheIdentifier, ImageScalerSettings ss) {
        // Possibly we should clear all entries starting with this URL, but that may
        // be expensive to do if the cache is large.
        /*        
        List<?> keys = cache.getKeysNoDuplicateCheck();
        for (Object key : keys) {
            if (key.toString().startsWith(scaledCacheIdentifier + "|")) {
                cache.remove(key);
            }
        }
        */
        
        // For now we just clear everything from this cache
        
        // In the ImageScaleController use case the ImageFetcher will still
        // have cached (full size) copies of all the other images, so scaling
        // will have to re-occur, but at least there's not another network request.
        this.clearAllCache();
    }

    @Override
    public void clearAllCache() {
        Cache cache = appCacheManager.getCache(CACHE);
        cache.removeAll();
    }
}
