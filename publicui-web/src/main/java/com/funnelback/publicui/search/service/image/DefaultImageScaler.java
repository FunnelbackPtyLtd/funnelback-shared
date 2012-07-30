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

		String processedKey = scaledCacheIdentifier + "|" + settings.getWidth()
				+ "|" + settings.getHeight() + "|"
				+ settings.getType() + "|" + settings.getFormat();

		log.trace("Scaling " + scaledCacheIdentifier + " to cache with key " + processedKey);

		
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
		
		cache.put(new Element(processedKey, scaledImageBytes.toByteArray()));

		return (byte[])cache.get(processedKey).getValue();
	}
}
