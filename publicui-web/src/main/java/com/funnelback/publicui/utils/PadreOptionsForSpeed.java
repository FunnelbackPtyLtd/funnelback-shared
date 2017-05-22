package com.funnelback.publicui.utils;

import static com.funnelback.common.padre.QueryProcessorOptionKeys.BB;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.CNTO;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.COLLAPSING;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.CONTEXTUAL_NAVIGATION;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.COUNTGBITS;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.COUNTINDEXEDTERMS;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.COUNT_DATES;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.COUNT_URLS;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.DAAT_TIMEOUT;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.DIVERSITY_RANK_LIMIT;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.EXPLAIN;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.GEOSPATIAL_RANGES;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.MBL;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.NEARDUP;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.QSUP;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.REPETITIOUSNESS_FACTOR;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.RMCF;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.RMRF;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.SAME_COLLECTION_SUPPRESSION;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.SAME_META_SUPPRESSION;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.SBL;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.SCO;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.SERVICE_VOLUME;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.SF;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.SM;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.SORT;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.SSS;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.STEM;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.SUM;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.SUMBYGROUP;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.TITLE_DUP_FACTOR;
import static com.funnelback.common.padre.QueryProcessorOptionKeys.VSIMPLE;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import lombok.AllArgsConstructor;
import lombok.Getter;
public class PadreOptionsForSpeed {
    
    @AllArgsConstructor
    public static class OptionAndValue{
        @Getter private final String option;
        @Getter private final String value;
    }
    
    /**
     * None of these options will affect the result set (un-ordered).
     * 
     * @return
     */
    public List<OptionAndValue> getOptionsThatDoNotAffectResultSetAsPairs() {
        //If this is to be used else where we should set
        // -countIndexedTerms, "[FunUnusedMetaClass]
        return ImmutableList.of(
            new OptionAndValue(VSIMPLE, "1"), // Turning on vsimple reduces ranking complexity, this should be done early so other
                          // other options can override what this sets.
            new OptionAndValue(DAAT_TIMEOUT, "0"), // Turning off daat timeout speeds up query processing.
            new OptionAndValue(SCO, "1"), //no ranking 
            new OptionAndValue(RMCF, ""),     // We don't need rmcf for this call override it if set.
            new OptionAndValue(CNTO, "0.001"),  // Don't let contextual nav run for too long if it someh   ow get enabled
            new OptionAndValue(CONTEXTUAL_NAVIGATION, "false"), // Turn of contextual nav we don't need it.
            new OptionAndValue(GEOSPATIAL_RANGES, "false"), // We don't need to work this out for the counts.
            new OptionAndValue(RMRF, "[FunUnusedMetaClass]"),  // We don't need to know ranges
            new OptionAndValue(SUM, "[FunUnusedMetaClass]"), //This does not need to be on
            new OptionAndValue(SUMBYGROUP, "[FunUnusedMetaClass]:[FunUnusedMetaClass]"),
            // I don't think MBL needs to be set as the default value for SF is to get no metadata so by default
            // we would get no metadata and so setting MBL is redundent in this case and in the case metadata is requested
            // it is just in the way.
            new OptionAndValue(SBL, "1"), //Don't need a summary
            new OptionAndValue(SF, "[FunUnusedMetaClass]"), //Don't show summary fields.
            new OptionAndValue(COUNTINDEXEDTERMS, "[FunUnusedMetaClass]"), //don't count indexed terms.
            new OptionAndValue(SM, "off"), //Try to disable summaries
            new OptionAndValue(EXPLAIN, "false"),
            new OptionAndValue(COUNT_DATES, ""), //Setting this to empty turns off counting dates.
            new OptionAndValue(COUNTGBITS, ""), //Setting this to empty turns it off.
            new OptionAndValue(COUNT_URLS, ""), //Emptu count_urls turns of url counting.
            new OptionAndValue(SORT, "") //Turn of sorting
            );
    }
    
    /**
     * Options which speed things up that do not reduce the result set (un-ordered) size.
     * 
     * <p>Things like duplicate detection are turned off</p>
     * 
     * @return
     */
    public List<OptionAndValue> getOptionsToTurnOfReducingResultSetAsPairs() {
        //If this is to be used else where we should set
        // -countIndexedTerms, "[FunUnusedMetaClass]
        return ImmutableList.of(
            new OptionAndValue(DIVERSITY_RANK_LIMIT, "10"), //scan less results for diversification.
            new OptionAndValue(SSS, "0"), //Turn off same site suppression
            new OptionAndValue(NEARDUP, "1"), //Setting to 1 turns it off
            new OptionAndValue(REPETITIOUSNESS_FACTOR, "1"), //Setting to 1 turns it off
            new OptionAndValue(SAME_COLLECTION_SUPPRESSION, "0"), //Setting to 0 turns it off
            new OptionAndValue(SAME_META_SUPPRESSION, "1"), //Setting to 1 turns it off
            new OptionAndValue(TITLE_DUP_FACTOR, "1"), //Setting to 1 turns it off
            new OptionAndValue(COLLAPSING, "off")
            );
    }
    
    /**
     * Options that will speed up the query processor but may result in the result 
     * set having less results for the query e.g. because spelling is turned off. 
     * 
     * @return
     */
    public List<OptionAndValue> getOptionsThatMayReduceResultSetAsPairs() {
        //If this is to be used else where we should set
        // 
        return ImmutableList.of(
            new OptionAndValue(BB, "false"), // Turn of best bets
            new OptionAndValue(QSUP, "off"), // We don't want blending
            new OptionAndValue(STEM, "0")    // We don't want stemming.
            );
    }
    
    /**
     * Returns the option to set a 'high' service volume.
     * 
     * This is the default, but in some systems (CA, AA) we need to ensure
     * it is not being overwritten by the collection's config because we
     * rely on the 'high' setting.
     */
    public OptionAndValue getHighServiceVolumeOptionAsPair() {
        return new OptionAndValue(SERVICE_VOLUME, ""); /* Intentionally empty */
    }

    /**
     * None of these options will affect the result set (un-ordered).
     * 
     * @return
     */
    public List<String> getOptionsThatDoNotAffectResultSet() {
        return this.getOptionsThatDoNotAffectResultSetAsPairs()
            .stream().map(this::toPadreCMDLineArgument).collect(Collectors.toList());
    }
    
    private String toPadreCMDLineArgument(OptionAndValue optionAndValue) {
        return "-" + optionAndValue.getOption() + "=" + optionAndValue.getValue();
    }
    
    /**
     * Options which speed things up that do not reduce the result set (un-ordered) size.
     * 
     * <p>Things like duplicate detection are turned off</p>
     * 
     * @return
     */
    public List<String> getOptionsToTurnOfReducingResultSet() {
        return this.getOptionsToTurnOfReducingResultSetAsPairs()
            .stream().map(this::toPadreCMDLineArgument).collect(Collectors.toList());
    }
    
    /**
     * Options that will speed up the query processor but may result in the result 
     * set having less results for the query e.g. because spelling is turned off. 
     * 
     * @return
     */
    public List<String> getOptionsThatMayReduceResultSet() {
        return getOptionsThatMayReduceResultSetAsPairs()
            .stream().map(this::toPadreCMDLineArgument).collect(Collectors.toList());
    }
    
    /**
     * Returns the option to set a 'high' service volume.
     * 
     * This is the default, but in some systems (CA, AA) we need to ensure
     * it is not being overwritten by the collection's config because we
     * rely on the 'high' setting.
     */
    public String getHighServiceVolumeOption() {
        return this.toPadreCMDLineArgument(this.getHighServiceVolumeOptionAsPair());
    }
    
    
}
