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
import com.funnelback.publicui.contentauditor.UrlScopeFill;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.MetadataFieldFill;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.wcag.checker.FailureType;

import lombok.extern.log4j.Log4j2;

/**
 * <p>Configure facets for the accessibility auditor.</p>
 * 
 * <p>Will discard any existing facet configuration as user defined
 * facets are not displayed in the accessibility auditor.</p>
 * 
 * @author nguillaumin@funnelback.com
 *
 */
@Log4j2
@Component("accessibilityAuditorConfigureFacets")
public class ConfigureFacets extends AbstractAccessibilityAuditorInputProcessor {

    /** ID of the URL drill down facet. Will be localized client side */
    private static final String URL_FACET_ID = "URL";
    
    private final FacetedNavigationConfig facetedNavigationConfig;
    
    public ConfigureFacets() {
        // Facet on success criteria (e.g. 1.2.3)
        Stream<String> successCriteria = Arrays.asList(FailureType.values())
            .stream()
            .map(type -> Stream.of(Names.successCriterion(type)))
            .flatMap(Function.identity())
            .map(Metadata::getName);

        // Facet on issue type
        Stream<String> issueTypes = Arrays.asList(FailureType.values())
            .stream()
            .map(type -> Stream.of(Names.issueTypes(type)))
            .flatMap(Function.identity())
            .map(Metadata::getName);

        // Facet on other metadata
        Stream<String> other = Stream.of(
            Names.profile().getName(),
            Names.domain().getName(),
            Names.affectedBy().getName(),
            "f");
        
        // Build our facet definitions and QPOs
        List<String> rmcf = new ArrayList<>();
        List<FacetDefinition> facetDefinitions = Stream
            .of(issueTypes, successCriteria, other)
            .flatMap(Function.identity())
            .map(Metadata::getMetadataClass)
            .map(metadataClass -> { rmcf.add(metadataClass); return metadataClass; })
            .map(this::createMetadataFieldFillFacetDefinition)
            .collect(Collectors.toList());
        
        // URL drill down facet
        facetDefinitions.add(createURLScopeFillFacetDefinition());
        
        // Build the -rmcf QPO for the facets
        String rmcfOptionValue = rmcf.stream()
            .collect(Collectors.joining(","));

        // -rmcf for metadata based facets, -count_urls for URLScopeFill facet
        // FIXME: -count_urls needs to be dynamic FUN-9043
        facetedNavigationConfig = new FacetedNavigationConfig(
            String.format("-rmcf=[%s] -count_urls=10",  rmcfOptionValue), facetDefinitions);
        
        log.debug("Initialised with QPO {} and facets: {}", facetedNavigationConfig.getQpOptions(),
            facetedNavigationConfig.getFacetDefinitions()
                .stream()
                .map(FacetDefinition::toString)
                .collect(Collectors.joining(System.getProperty("line.separator"))));
    }
    
    @Override
    public void processAccessibilityAuditorTransaction(SearchTransaction transaction) throws InputProcessorException {
        // Always override any faceted nav. config (active profile or collection level)
        Profile profile = transaction.getQuestion()
            .getCollection()
            .getProfiles()
            .get(transaction.getQuestion().getProfile());
        if (profile != null) {
            profile.setFacetedNavConfConfig(null);
        }
        
        transaction.getQuestion()
            .getCollection()
            .setFacetedNavigationConfConfig(null);

        transaction.getQuestion()
            .getCollection()
            .setFacetedNavigationLiveConfig(facetedNavigationConfig);
    }
    
    /**
     * Create a metadata field fill facet definition
     * 
     * @param field Field to facet over, also used as label
     * @return Facet definition
     */
    private FacetDefinition createMetadataFieldFillFacetDefinition(String field) {
        List<CategoryDefinition> categoryDefinitions = new ArrayList<CategoryDefinition>();
        MetadataFieldFill fill = new MetadataFieldFill(field);
        fill.setLabel(field);
        fill.setFacetName(field);
        categoryDefinitions.add(fill);
        
        return new FacetDefinition(field, categoryDefinitions);
    }
    
    /**
     * Create a URL drill down facet definition
     * 
     * @return Facet definition
     */
    private FacetDefinition createURLScopeFillFacetDefinition() {
        List<CategoryDefinition> categoryDefinitions = new ArrayList<CategoryDefinition>();
        UrlScopeFill fill = new UrlScopeFill("");
        fill.setLabel(URL_FACET_ID);
        fill.setFacetName(URL_FACET_ID);
        categoryDefinitions.add(fill);
        
        return new FacetDefinition(URL_FACET_ID, categoryDefinitions);
    }

}
