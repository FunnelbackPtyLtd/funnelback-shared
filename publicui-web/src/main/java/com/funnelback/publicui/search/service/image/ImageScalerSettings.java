package com.funnelback.publicui.search.service.image;

import lombok.Getter;
import lombok.Setter;

public class ImageScalerSettings {

	@Getter @Setter private Integer width;
	
	@Getter @Setter private Integer height;
	
	/**
	 * How the image should be scaled or cropped to the specified size.
	 */
	@Getter @Setter private ScaleType type;
	
	/**
	 * The format (png, jpg etc.) the scaled image should be in.
	 */
	@Getter @Setter private String format;
	
	/**
	 * The possible types of cropping which can occur
	 */
	enum ScaleType {
		// Note - Crop names match names in thumbailinator's Positions enum
		crop_top_left, crop_top_center, crop_top_right,
		crop_center_left, crop_center, crop_center_right,
		crop_bottom_left, crop_bottom_center, crop_bottom_right,
		keep_aspect, ignore_aspect
	}
}
