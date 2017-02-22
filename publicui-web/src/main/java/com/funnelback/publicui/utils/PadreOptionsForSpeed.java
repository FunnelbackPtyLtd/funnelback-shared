package com.funnelback.publicui.utils;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class PadreOptionsForSpeed {

    /**
     * None of these options will affect the result set (un-ordered).
     * 
     * @return
     */
    public List<String> getOptionsThatDoNotAffectResultSet() {
        //If this is to be used else where we should set
        // -countIndexedTerms=[FunUnusedMetaClass]
        return ImmutableList.of(
            "-vsimple=1", // Turning on vsimple reduces ranking complexity, this should be done early so other
                          // other options can override what this sets.
            "-daat_timeout=0", // Turning off daat timeout speeds up query processing.
            "-sco=1", //no ranking 
            "-rmcf=[]",     // We don't need rmcf for this call override it if set.
            "-cnto=0.001",  // Don't let contextual nav run for too long if it somehow get enabled
            "-contextual_navigation=false", // Turn of contextual nav we don't need it.
            "-geospatial_ranges=false", // We don't need to work this out for the counts.
            "-rmrf=[FunUnusedMetaClass]",  // We don't need to know ranges
            "-sum=[FunUnusedMetaClass]", //This does not need to be on
            "-sumByGroup=[FunUnusedMetaClass]",
            "-MBL=1", //We don't need to see metadata values
            "-SBL=1", //Don't need a summary
            "-SF=[FunUnusedMetaClass]", //Don't show summary fields.
            "-countIndexedTerms=[FunUnusedMetaClass]", //don't count indexed terms.
            "-SM=off", //Try to disable summaries
            "-explain=false",
            "-count_dates=", //Setting this to empty turns off counting dates.
            "-countgbits=", //Setting this to empty turns it off.
            "-count_urls=", //Emptu count_urls turns of url counting.
            "-sort=" //Turn of sorting
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
        // -countIndexedTerms=[FunUnusedMetaClass]
        return ImmutableList.of(
            "-diversity_rank_limit=10", //scan less results for diversification.
            "-SSS=0", //Turn off same site suppression
            "-neardup=1", //Setting to 1 turns it off
            "-repetitiousness_factor=1", //Setting to 1 turns it off
            "-same_collection_suppression=0", //Setting to 0 turns it off
            "-same_collection_suppression=1", //Setting to 1 turns it off
            "-title_dup_factor=1", //Setting to 1 turns it off
            "-collapsing=off"
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
            "-bb=false", // Turn of best bets
            "-qsup=off", // We don't want blending
            "-stem=0"    // We don't want stemming.
            );
    }
    
    
    
}
