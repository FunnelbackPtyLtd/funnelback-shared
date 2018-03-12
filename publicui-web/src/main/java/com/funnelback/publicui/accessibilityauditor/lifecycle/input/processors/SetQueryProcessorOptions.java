package com.funnelback.publicui.accessibilityauditor.lifecycle.input.processors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.funnelback.common.accessibility.FailureConfidence;
import com.funnelback.common.filter.accessibility.Metadata;
import com.funnelback.common.filter.accessibility.Metadata.Names;
import com.funnelback.common.function.StreamUtils;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;
import com.funnelback.wcag.checker.AccessibilityChecker.Level;
import com.funnelback.wcag.model.WCAG20Principle;
import com.funnelback.wcag.model.WCAG20Technique;

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
    protected void processAccessibilityAuditorTransaction(SearchTransaction st) throws InputProcessorException {
        if (SearchTransactionUtils.hasQuestion(st)
            && SearchQuestionType.ACCESSIBILITY_AUDITOR.equals(st.getQuestion().getQuestionType())) {

            st.getQuestion().setLogQuery(Optional.ofNullable(false));
            st.getQuestion().getDynamicQueryProcessorOptions().addAll(options);

            new AccessibilityAuditorDaatOption().getDaatOption(st.getQuestion().getCollection().getConfiguration())
                .ifPresent(option -> st.getQuestion().getDynamicQueryProcessorOptions().add(option));
        }
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

        Stream<String> failureTypes = Arrays.asList(FailureConfidence.values())
            .stream()
            .map(type -> Stream.of(Names.failureTypesOccurrences(type)))
            .flatMap(Function.identity())
            .map(Metadata::getName);

        Stream<String> principles = Arrays.asList(WCAG20Principle.values())
            .stream()
            .map(principle -> Stream.of(Names.principleOccurrences(principle)))
            .flatMap(Function.identity())
            .map(Metadata::getName);
        
        Stream<String> scFailuresByLevel = Arrays.asList(Level.values())
            .stream()
            .map(Metadata.Names::occurrencesOfFailingSuccessCriteriaByLevel)
            .map(Metadata::getName);

        Stream<String> other = Stream.of(
            Names.occurencesOfFailingSuccessCriteria(),
            Names.setOfFailingPrinciples(),
            Names.setOfFailingSuccessCriterions(),
            Names.setOfFailingTechniques(),
            Names.techniquesAffectedBy(),
            Names.occurrencesOfFailingTechniques(),
            Names.domain(),
            Names.passedLevels(),
            Names.explicitFailedLevels(),
            Names.isAffected(),
            Names.unaffected(),
            Names.checked(),
            Names.format(),
            Names.occurencesOfUniqueFailingSuccessCriterions())
            .map(Metadata::getName);

        String sfOptionValue = Stream.of(failureTypes, principles, scFailuresByLevel, other)
            .flatMap(Function.identity())
            .map(Metadata::getMetadataClass)
            .collect(Collectors.joining(","));

        return String.format("-SF=[%s]", sfOptionValue);
    }

    /**
     * Set the MBL option. The longest value for a metadata will be when
     * a document is affected by all techniques.
     * 
     * @return PADRE <code>-MBL</code> option
     */
    String getMBLOption() {
        
        AtomicInteger i = new AtomicInteger();
        int totalSize = StreamUtils.ofNullable(WCAG20Technique.values())
        .map(v -> v.id)
        .map(name -> name.length())
        .map(length -> length++) // Add one to the length to account for the metadata separator e.g. '|'
        .mapToInt(Integer::intValue).sum()
        + 1; // Add one to account for a NUL in C.

        return "-MBL=" + Integer.toString(Integer.max(DEFAULT_MBL, totalSize));
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
        // FIXME: add support for profiles.
        for (String grouping : new String[] {
                        Names.domain().getName()
        }) {
            sums.add(String.format("[%s.+Occurrences.*]:[%s]", Metadata.getMetadataClassPrefix(), Metadata.getMetadataClass(grouping)));
            sums.add(String.format("[%sAffected.+]:[%s]", Metadata.getMetadataClassPrefix(), Metadata.getMetadataClass(grouping)));
            sums.add(String.format("[%s]:[%s]", Metadata.Names.isAffected().asMetadataClass(), Metadata.getMetadataClass(grouping)));
            sums.add(String.format("[%s]:[%s]", Metadata.Names.unaffected().asMetadataClass(), Metadata.getMetadataClass(grouping)));
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
        // FIXME: support profiles
        for (String grouping : new String[] {
                        Names.domain().getName()
        }) {
            // Type of checkers
            //Not sure we need anything here.
        }

        // Count of unique domains per profile
        // FIXME: support profiles.
//        counts.add(String.format("[%s]:[%s]",
//            Metadata.getMetadataClass(Metadata.Names.domain().getName()),
//            Metadata.getMetadataClass(Metadata.Names.profile().getName())));

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
     * By default, sort by occurrences in order to display "top documents".
     * 
     * Can be overwritten by passing in 'sort=' as a query parameter.
     * 
     * @return PADRE <code>-sort</code> option
     */
    private String getSortOption() {
        return "-sort=dmeta" + Metadata.getMetadataClass(Metadata.Names.occurrencesOfFailingTechniques().getName());
    }

}
