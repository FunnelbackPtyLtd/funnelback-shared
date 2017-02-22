package com.funnelback.publicui.utils;

import java.util.List;

import com.google.common.collect.ImmutableList;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.*;
public class PadreOptionsForSpeed {

    /**
     * None of these options will affect the result set (un-ordered).
     * 
     * @return
     */
    public List<String> getOptionsThatDoNotAffectResultSet() {
        //If this is to be used else where we should set
        // -countIndexedTerms + "=[FunUnusedMetaClass]
        return ImmutableList.of(
            "-" + VSIMPLE + "=1", // Turning on vsimple reduces ranking complexity, this should be done early so other
                          // other options can override what this sets.
            "-" + DAAT_TIMEOUT + "=0", // Turning off daat timeout speeds up query processing.
            "-" + SCO + "=1", //no ranking 
            "-" + RMCF + "=[]",     // We don't need rmcf for this call override it if set.
            "-" + CNTO + "=0.001",  // Don't let contextual nav run for too long if it somehow get enabled
            "-" + CONTEXTUAL_NAVIGATION + "=false", // Turn of contextual nav we don't need it.
            "-" + GEOSPATIAL_RANGES + "=false", // We don't need to work this out for the counts.
            "-" + RMRF + "=[FunUnusedMetaClass]",  // We don't need to know ranges
            "-" + SUM + "=[FunUnusedMetaClass]", //This does not need to be on
            "-" + SUMBYGROUP + "=[FunUnusedMetaClass]",
            "-" + MBL + "=1", //We don't need to see metadata values
            "-" + SBL + "=1", //Don't need a summaryW
            "-" + SF + "=[FunUnusedMetaClass]", //Don't show summary fields.
            "-" + COUNTINDEXEDTERMS + "=[FunUnusedMetaClass]", //don't count indexed terms.
            "-" + SM + "=off", //Try to disable summaries
            "-" + EXPLAIN + "=false",
            "-" + COUNT_DATES + "=", //Setting this to empty turns off counting dates.
            "-" + COUNTGBITS + "=", //Setting this to empty turns it off.
            "-" + COUNT_URLS + "=", //Emptu count_urls turns of url counting.
            "-" + SORT + "=" //Turn of sorting
            );
    }
    
    /**
     * Options which speed things up that do not reduce the result set (un-ordered) size.
     * 
     * <p>Things like duplicate detection are turned off</p>
     * 
     * @return
     */
    public List<String> getOptionsToTurnOfReducingResultSet() {
        //If this is to be used else where we should set
        // -countIndexedTerms + "=[FunUnusedMetaClass]
        return ImmutableList.of(
            "-" + DIVERSITY_RANK_LIMIT + "=10", //scan less results for diversification.
            "-" + SSS + "=0", //Turn off same site suppression
            "-" + NEARDUP + "=1", //Setting to 1 turns it off
            "-" + REPETITIOUSNESS_FACTOR + "=1", //Setting to 1 turns it off
            "-" + SAME_COLLECTION_SUPPRESSION + "=0", //Setting to 0 turns it off
            "-" + SAME_META_SUPPRESSION + "=1", //Setting to 1 turns it off
            "-" + TITLE_DUP_FACTOR + "=1", //Setting to 1 turns it off
            "-" + COLLAPSING + "=off"
            );
    }
    
    /**
     * Options that will speed up the query processor but may result in the result 
     * set having less results for the query e.g. because spelling is turned off. 
     * 
     * @return
     */
    public List<String> getOptionsThatMayReduceResultSet() {
        //If this is to be used else where we should set
        // 
        return ImmutableList.of(
            "-" + BB + "=false", // Turn of best bets
            "-" + QSUP + "=off", // We don't want blending
            "-" + STEM + "=0"    // We don't want stemming.
            );
    }
    
    
    
}
