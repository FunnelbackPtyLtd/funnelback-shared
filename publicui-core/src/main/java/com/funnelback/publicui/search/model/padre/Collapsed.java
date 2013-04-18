package com.funnelback.publicui.search.model.padre;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Information about collapsed results, for a given
 * {@link Result}
 * 
 * @since 12.5
 */
@AllArgsConstructor
@NoArgsConstructor
public class Collapsed {

    /**
     * Common signature for all collapsed documents
     */
    @Getter @Setter private String signature;
    
    /**
     * Number of collapsed documents
     */
    @Getter @Setter private int count;
    
}
