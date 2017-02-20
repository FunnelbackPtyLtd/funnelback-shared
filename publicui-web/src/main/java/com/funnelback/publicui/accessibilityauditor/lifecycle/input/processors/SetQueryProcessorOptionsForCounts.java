package com.funnelback.publicui.accessibilityauditor.lifecycle.input.processors;

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Component;

import com.funnelback.common.padre.QueryProcessorOptionKeys;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.google.common.collect.ImmutableList;

/**
 * Sets the relevant query processor options needed to generate
 * Accessibility Auditor Acknowledgement counts.
 * 
 */
@Log4j2
@Component("accessibilityAuditorAcknowledgmentCountsSetQueryProcessorOptions")
public class SetQueryProcessorOptionsForCounts extends AbstractAccessibilityAuditorAcknowledgmentCountsInputProcessor {

    public static final String AA_ACKNOWLEDGMENT_COUNT_METADATA_CLASS = "FunAAAcknowledgments"; 
    
    
    private final List<String> options;

    public SetQueryProcessorOptionsForCounts() {
        options = ImmutableList.<String>builder()
            .add(getLogOption())
            .addAll(getOptionsForSpeedThatDoNotAffectResultSet())
            .add(getIndexedTermsOptions())
            .build();
        
        log.debug("Initialised with options: {}", options.stream().collect(Collectors.joining(System.getProperty("line.separator"))));
    }
    
    @Override
    protected void processAccessibilityAuditorTransaction(SearchTransaction st) throws InputProcessorException {
        //Clear all previous options as they risk slowing us down.
        st.getQuestion().getDynamicQueryProcessorOptions().addAll(options);
    }
    
    /**
     * Disable logging to avoid
     * polluting analytics with AA requests
     * 
     * @return PADRE <code>log</code> option
     */
    private String getLogOption() {
        return "-" + QueryProcessorOptionKeys.LOG + "=off";
    }
    
    /**
     * Get query processor options for running the query quickly.
     * 
     * <p>This aims to override as many setting that can slow down padre as possible.
     * Options set in collection.cfg will be overridden by these. This does not intend 
     * to drastically change what is in the result set (un-ordered). This still intends 
     * for DLS to work, for gscope restrictions to work, xscope restrictions, clive etc.
     * It does however alter the query so that blending, synonyms, and searching over 
     * implicit fields (e.g. looking in document titles) is turned off. </p>
     * 
     * </p>
     * 
     * @return
     */
    private List<String> getOptionsForSpeedThatDoNotAffectResultSet() {
        //If this is to be used else where we should set
        // -countIndexedTerms=[FunUnusedMetaClass]
        return ImmutableList.of(
            "-vsimple=1", // Turning on vsimple reduces ranking complexity, this should be done early so other
                          // other options can override what this sets.
            
            "-daat=10000000",  // By default the count terms option is very fast so going 10M deep is safe
                               // probably takes about 1s to run.
                               // Customised collections with expensive DLS may need to dial this back.
            
            "-daat_timeout=0", // Turning off daat timeout speeds up query processing.
            
            "-sco=1[FunUnusedMetaClass]",// The result set will be just that a set (ie unordered no ranking will be done)
                                      // Setting the metadata class to FunDoesNotExist ensures we never actually look 
                                      // inside of metadata for the query, speeding up the query.
            
            "-num_ranks=1", // We are after counts and so we don't need to see results.
            "-rmcf=[]",     // We don't need rmcf for this call override it if set.
            "-cnto=0.001",  // Don't let contextual nav run for too long if it somehow get enabled
            "-contextual_navigation=false", // Turn of contextual nav we don't need it.
            "-geospatial_ranges=false", // We don't need to work this out for the counts.
            "-countUniqueByGroup=[FunUnusedMetaClass]:[FunUnusedMetaClass]", //We don't need this so turn it off
            "-rmrf=[FunUnusedMetaClass]",  // We don't need to know ranges
            "-sum=[FunUnusedMetaClass]", //This does not need to be on
            "-sumByGroup=[FunUnusedMetaClass]",
            "-MBL=1", //We don't need to see metadata values
            "-SBL=1", //Don't need a summary
            "-SF=[FunUnusedMetaClass]", //Don't show summary fields.
            "-SM=off", //Try to disable summaries
            "-bb=false", //turn of best bets
            "-explain=false",
            "-qsup=off",  // We don't want blemding
            "-diversity_rank_limit=10", //scan less results for diversification.
            "-SSS=0", //Turn of same site suppression
            "-neardup=1", //Setting to 1 turns it off
            "-repetitiousness_factor=1", //Setting to 1 turns it off
            "-same_collection_suppression=0", //Setting to 0 turns it off
            "-same_collection_suppression=1", //Setting to 1 turns it off
            "-title_dup_factor=1", //Setting tp 1 turns it off
            "-spelling=false", //Turn of spelling suggestions
            "-count_dates=", //Setting this to empty turns off counting dates.
            "-countgbits=", //Setting this to empty turns it off.
            "-count_urls=", //Emptu count_urls turns of url counting.
            "-sort=" //Turn of sorting we don't need it.
            
            );
    }
    
    /**
     * Get query processor option for working out the counts.
     */
    
    private String getIndexedTermsOptions() {
        return "-countIndexedTerms=[" + AA_ACKNOWLEDGMENT_COUNT_METADATA_CLASS + "]";
    }

}
