package com.funnelback.publicui.accessibilityauditor.lifecycle.input.processors;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.funnelback.common.filter.accessibility.Metadata;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.PadreOptionsForSpeed;
import com.google.common.collect.ImmutableList;

import lombok.extern.log4j.Log4j2;

/**
 * Sets the relevant query processor options needed to generate
 * Accessibility Auditor Acknowledgement counts.
 * 
 */
@Log4j2
@Component("accessibilityAuditorAcknowledgementCountsSetQueryProcessorOptions")
public class SetQueryProcessorOptionsForCounts extends AbstractAccessibilityAuditorAcknowledgementCountsInputProcessor {

    public static final String AA_ACKNOWLEDGEMENT_COUNT_METADATA_CLASS = 
        Metadata.getMetadataClass(Metadata.Names.acknowledgements().getName()); 
    
    
    private final List<String> options;

    public SetQueryProcessorOptionsForCounts() {
        options = ImmutableList.<String>builder()
            .addAll(getOptionsForSpeed()) //add options to speed things up.
            .add(getIndexedTermsOptions())
            .build();
        
        log.debug("Initialised with options: {}", options.stream().collect(Collectors.joining(System.getProperty("line.separator"))));
    }
    
    @Override
    protected void processAccessibilityAuditorTransaction(SearchTransaction st) throws InputProcessorException {

        options.add(new AccessibilityAuditorDaatOption().getDaatOption(st.getQuestion()));
        st.getQuestion().getDynamicQueryProcessorOptions().addAll(options);
        st.getQuestion().setLogQuery(Optional.ofNullable(false));

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
    private List<String> getOptionsForSpeed() {
        PadreOptionsForSpeed optionsForSpeed = new PadreOptionsForSpeed();
        return ImmutableList.<String>builder()
            .addAll(optionsForSpeed.getOptionsThatDoNotAffectResultSet())
            .addAll(optionsForSpeed.getOptionsThatMayReduceResultSet())
            .addAll(optionsForSpeed.getOptionsToTurnOfReducingResultSet())
            .build();
    }
    
    /**
     * Get query processor option for working out the counts.
     */
    
    private String getIndexedTermsOptions() {
        return "-countIndexedTerms=[" + AA_ACKNOWLEDGEMENT_COUNT_METADATA_CLASS + "]";
    }

}
