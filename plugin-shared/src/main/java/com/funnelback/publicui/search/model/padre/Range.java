package com.funnelback.publicui.search.model.padre;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * A range of values (used for numeric metadata ranges)
 * 
 * @since 11.0
 *
 */
@RequiredArgsConstructor
@AllArgsConstructor
public class Range {

    /** The minimum value in the range */
    @Getter @Setter private Double minimum;
    
    /** The maximum value in the range */
    @Getter @Setter private Double maximum;
}
