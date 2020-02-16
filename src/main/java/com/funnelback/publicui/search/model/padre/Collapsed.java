package com.funnelback.publicui.search.model.padre;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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
    
    /**
     * Column from the signature file on which
     * the result was collapsed
     */
    @Getter @Setter private String column;

    /** List of collapsed results */
    @Getter final private List<Result> results = new ArrayList<>();


}
