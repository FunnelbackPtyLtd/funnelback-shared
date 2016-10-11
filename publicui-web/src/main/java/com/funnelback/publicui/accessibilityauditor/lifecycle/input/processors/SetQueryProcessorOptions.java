package com.funnelback.publicui.accessibilityauditor.lifecycle.input.processors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.funnelback.common.filter.accessibility.Metadata;
import com.funnelback.common.filter.accessibility.Metadata.Names;
import com.funnelback.common.function.StreamUtils;
import com.funnelback.common.padre.QueryProcessorOptionKeys;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.wcag.checker.AccessibilityChecker;
import com.funnelback.wcag.checker.CheckerClasses;
import com.funnelback.wcag.checker.FailureType;
import com.funnelback.wcag.model.WCAG20Principle;

import lombok.extern.log4j.Log4j2;

/**
 * Sets the relevant query processor options needed to generate
 * Accessibility Auditor reports
 * 
 * @author nguillaumin@funnelback.com
 */
@Log4j2
@Component("accessibilityAuditorSetQueryProcessorOptions")
public class SetQueryProcessorOptions extends AbstractAccessibilityAuditorInputProcessor {

    /** Default value for PADRE's MBL option */
    private static final int DEFAULT_MBL = 250;
    
    private final List<String> options = new ArrayList<>();

    public SetQueryProcessorOptions() {
        options.add(getLogOption());
        options.add(getSMOption());
        options.add(getSFOption());
        options.add(getMBLOption());
        options.add(getSumOption());
        options.add(getSumByGroupOption());
        options.add(getSumByGroupSensitiveOption());
        options.add(getCountByGroupOption());
        options.add(getCountUniqueByGroupSensitiveOption());
        options.add(getRmcSensitiveOption());
        options.add(getSortOption());
        
        log.debug("Initialised with options: {}", options.stream().collect(Collectors.joining(System.getProperty("line.separator"))));
    }
    
    @Override
    public void processAccessibilityAuditorTransaction(SearchTransaction st) throws InputProcessorException {
        if (SearchTransactionUtils.hasQuestion(st)
            && SearchQuestionType.ACCESSIBILITY_AUDITOR.equals(st.getQuestion().getQuestionType())) {

            st.getQuestion().getDynamicQueryProcessorOptions().addAll(options);
            st.getQuestion().getAdditionalParameters().remove("sort");
        }
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
     * Get the SM option to output metadata fields
     * 
     * @return PADRE <code>-SM</code> option
     */
    private String getSMOption() {
        return "-SM=both";
    }
    
    /**
     * Get the rmc_sensitive option to preserve case in metadata
     * 
     * @return PADRE <code>-rmc_sensitve</code> option
     */
    private String getRmcSensitiveOption() {
        return "-rmc_sensitive=on";
    }
    
    /**
     * Ge the SF option with the list of metadata fields to return with each
     * result
     * 
     * @return PADRE <code>-SF</code> option
     */
    private String getSFOption() {
        Stream<String> failureTypesAffected = Arrays.asList(FailureType.values())
            .stream()
            .map(type -> Stream.of(Names.failureTypeAffected(type)))
            .flatMap(Function.identity())
            .map(Metadata::getName);

        Stream<String> failureTypes = Arrays.asList(FailureType.values())
            .stream()
            .map(type -> Stream.of(Names.failureTypesOccurrences(type)))
            .flatMap(Function.identity())
            .map(Metadata::getName);

        Stream<String> successCriteria = Arrays.asList(FailureType.values())
            .stream()
            .map(type -> Stream.of(Names.successCriterion(type)))
            .flatMap(Function.identity())
            .map(Metadata::getName);
        
        Stream<String> principles = Arrays.asList(WCAG20Principle.values())
            .stream()
            .map(principle -> Stream.of(Names.principleOccurrences(principle)))
            .flatMap(Function.identity())
            .map(Metadata::getName);

        Stream<String> other = Stream.of(
            Names.profile(),
            Names.domain(),
            Names.principle(),
            Names.affectedBy(),
            Names.passedLevels(),
            Names.failedLevels(),
            Names.affected(),
            Names.unaffected(),
            Names.checked(),
            Names.occurrences(),
            Names.checks(),
            Names.checksPassed())
            .map(Metadata::getName);
        
        String sfOptionValue = Stream.of(failureTypesAffected, failureTypes, successCriteria, principles, other)
            .flatMap(Function.identity())
            .map(Metadata::getMetadataClass)
            .collect(Collectors.joining(","));

        return String.format("-SF=[%s]", sfOptionValue);
    }
    
    /**
     * Set the MBL option. The longest value for a metadata will be when
     * a document is affected by all checker types, meaning the
     * list of all checker classes joined together with a separator
     * 
     * @return PADRE <code>-MBL</code> option
     */
    private String getMBLOption() {
        List<AccessibilityChecker> checkers = Arrays.asList(CheckerClasses.allCheckerClasses)
            .stream()
            .map(StreamUtils::newInstance)
            .collect(Collectors.toList());

        // Collect maximum length for each failure type
        List<Integer> lengths = new ArrayList<>();
        
        for (FailureType failureType: FailureType.values()) {
            lengths.add(checkers.stream()
                .filter(checker -> checker.getFailureType().equals(failureType))
                .map(checker -> checker.getClass().getSimpleName())
                .collect(Collectors.joining("|"))   // Separator doesn't matter here, we care only about the length
                .length());
        }
        
        int mblOptionValue = lengths.stream()
            .max(Integer::max)
            .orElse(DEFAULT_MBL);   // Not supposed to happen, but default to something just in case
        
        // +1 to account for possible null terminator in PADRE
        return String.format("-MBL=%d", mblOptionValue + 1);
    }
    
    /**
     * Get the sum option, used to sum metadata values
     * 
     * @return PADRE <code>-sum</code> option
     */
    private String getSumOption() {
        // FIXME: Sum all AA metadata for now
        return String.format("-sum=[%s.+]", Metadata.getMetadataClassPrefix());
    }
    
    /**
     * Get the sumByGroup option to sum metadata values per group
     * 
     * @return PADRE <code>-sumByGroup</code> option
     */
    private String getSumByGroupOption() {
        List<String> sums = new ArrayList<>();
        
        // Group by domains & profiles
        for (String grouping: new String[] {
                        Names.domain().getName(), Names.profile().getName()
        }) {
            // FIXME: Not sure how to avoid hardcoding things here...
            sums.add(String.format("[%sOccurrences.*]:[%s]", Metadata.getMetadataClassPrefix(), Metadata.getMetadataClass(grouping)));
            sums.add(String.format("[%s.+Occurrences]:[%s]", Metadata.getMetadataClassPrefix(), Metadata.getMetadataClass(grouping)));
            sums.add(String.format("[%sAffected.+]:[%s]", Metadata.getMetadataClassPrefix(), Metadata.getMetadataClass(grouping)));
            sums.add(String.format("[%s(A|Un)ffected]:[%s]", Metadata.getMetadataClassPrefix(), Metadata.getMetadataClass(grouping)));
            sums.add(String.format("[%sChecked]:[%s]", Metadata.getMetadataClassPrefix(), Metadata.getMetadataClass(grouping)));
            sums.add(String.format("[%sPassedLevel[A]+]:[%s]", Metadata.getMetadataClassPrefix(), Metadata.getMetadataClass(grouping)));
        }
        
        String sumByGroupOptionValue = sums.stream().collect(Collectors.joining(","));
        
        return String.format("-sumByGroup=%s", sumByGroupOptionValue);
    }
    
    /**
     * Get the sum case sensitivity option to preserve
     * case in sums
     * 
     * @return PADRE <code>-countUniqueByGroupSensitive</code> option
     */
    private String getSumByGroupSensitiveOption() {
        return "-sumByGroupSensitive=on";
    }
    
    /**
     * Get the countUniqueByGroup option, used to count distinct
     * values of a metadata
     * 
     * @return PADRE <code>-countUniqueByGroup</code> option
     */
    private String getCountByGroupOption() {
        List<String> counts = new ArrayList<>();
        
        // Group by domains & profiles
        for (String grouping: new String[] {
                        Names.domain().getName(), Names.profile().getName()
        }) {
            // Type of checkers
            counts.add(String.format("[%s.+Types]:[%s]", Metadata.getMetadataClassPrefix(), Metadata.getMetadataClass(grouping)));
            
            // Success criterion value (e.g. 1.2.3), for each failure type
            for (FailureType type: FailureType.values()) {
                counts.add(String.format("[%s]:[%s]", Metadata.getMetadataClass(grouping), Metadata.getMetadataClass(Names.successCriterion(type).getName())));
            }
        }
        
        // Count of unique domains per profile
        counts.add(String.format("[%s]:[%s]",
            Metadata.getMetadataClass(Metadata.Names.domain().getName()),
            Metadata.getMetadataClass(Metadata.Names.profile().getName())));
        
        String countByGroupOptionValue = counts.stream().collect(Collectors.joining(","));
        
        return String.format("-countUniqueByGroup=%s", countByGroupOptionValue);
    }
    
    /**
     * Get the group count case sensitivity option to preserve
     * case in unique groups
     * 
     * @return PADRE <code>-countUniqueByGroupSensitive</code> option
     */
    private String getCountUniqueByGroupSensitiveOption() {
        return "-countUniqueByGroupSensitive=on";
    }
    
    /**
     * Always sort by occurrences in order to display "top documents"
     * 
     * FIXME: Perhaps let the user control the sort?
     * 
     * @return PADRE <code>-sort</code> option
     */
    private String getSortOption() {
        return "-sort=dmeta" + Metadata.getMetadataClass(Metadata.Names.occurrences().getName());
    }

}
