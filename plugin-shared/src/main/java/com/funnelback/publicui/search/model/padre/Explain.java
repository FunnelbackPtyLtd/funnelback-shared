package com.funnelback.publicui.search.model.padre;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Search explain plan used in the Content Optimiser.
 * 
 * @since 11.0
 */
@NoArgsConstructor
@AllArgsConstructor
public class Explain {

    /**
     * The final score for this result
     */
    @Getter public float finalScore;  
    
    /**
     * The number of constraints this result satisfied
     */
    @Getter public int consat;
    
    /**
     * The length ratio of this document relative to the average document, measured in content words 
     */
    @Getter public float lenratio;
    
    /**
     * <p>Map of float scores for each ranking feature.</p>
     * 
     * <p>Feature names are specified by their unique short name + id.</p>
     */
    @Getter public Map<CoolerWeighting, Float> featureScores;
}

