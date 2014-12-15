package com.funnelback.publicui.search.model.padre;

import groovy.transform.ToString;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The smallest bounding box the coordinates of all result within the daat limit.
 *
 * <p>Locations may be on the very edge of the box</p>
 */
@AllArgsConstructor
@ToString
public class GeoBoundingBox {

    @Getter private LatLong upperRight;
    @Getter private LatLong lowerLeft;
    
    @AllArgsConstructor
    @ToString
    public static class LatLong {
        @Getter private double latitude;
        @Getter private double longitude;
    }
    
}
