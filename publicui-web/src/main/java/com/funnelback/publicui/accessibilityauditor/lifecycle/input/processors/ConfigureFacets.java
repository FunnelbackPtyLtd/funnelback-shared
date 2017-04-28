package com.funnelback.publicui.accessibilityauditor.lifecycle.input.processors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.funnelback.common.filter.accessibility.Metadata;
import com.funnelback.common.filter.accessibility.Metadata.Names;
import com.funnelback.publicui.contentauditor.MissingMetadataFill;
import com.funnelback.publicui.contentauditor.UrlScopeFill;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.collection.delegate.OverrideFacetConfigCollection;
import com.funnelback.publicui.search.model.collection.delegate.OverrideFacetConfigDelegateProfile;
import com.funnelback.publicui.search.model.collection.delegate.OverrideProfilesConfigCollection;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.MetadataFieldFill;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

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
    
    /** ID of the facet containing missing metadata */
    private static final String MISSING_METADATA_FACET_ID = Metadata.getMetadataClassPrefix() + "Missing";
    
    private final FacetedNavigationConfig facetedNavigationConfig;
    
    public ConfigureFacets() {
        // Facet on issue type

        // Facet on other metadata
        Stream<String> other = Stream.of(
            Names.domain(),
            Names.setOfFailingPrinciples(),
            Names.setOfFailingSuccessCriterions(),
            Names.setOfFailingTechniques(),
            Names.techniquesAffectedBy(),
            Names.passedLevels(),
            Names.explicitFailedLevels(),
            Names.format())
            .map(Metadata::getName);
        
        // Build our facet definitions and QPOs
        List<String> rmcf = new ArrayList<>();
        List<FacetDefinition> facetDefinitions = Stream
            .of(other)
            .flatMap(Function.identity())
            .map(Metadata::getMetadataClass)
            .map(metadataClass -> { rmcf.add(metadataClass); return metadataClass; })
            .map(this::createMetadataFieldFillFacetDefinition)
            .collect(Collectors.toList());
        
        // URL drill down facet
        facetDefinitions.add(createURLScopeFillFacetDefinition());
        
        // Used the missing metadata facet to find documents with
        // missing "checked" metadata, to scope documents that were not audited
        facetDefinitions.add(createMissingMetadataFacetDefinition(MISSING_METADATA_FACET_ID));
        
        facetedNavigationConfig = new FacetedNavigationConfig(facetDefinitions);
        
        log.debug("Initialised facets: {}", facetedNavigationConfig.getFacetDefinitions()
                .stream()
                .map(FacetDefinition::toString)
                .collect(Collectors.joining(System.getProperty("line.separator"))));
    }
    
    @Override
    protected void processAccessibilityAuditorTransaction(SearchTransaction transaction) throws InputProcessorException {
        // Use delegate collections & profiles to avoid modifying the collection
        // object which is cached in EhCache
        Collection c = new OverrideFacetConfigCollection(
            transaction.getQuestion().getCollection(), facetedNavigationConfig, facetedNavigationConfig);

        Map<String, Profile> profiles = new HashMap<>();
        c.getProfiles().forEach(
            (profileId, profile) -> profiles.put(profileId, new OverrideFacetConfigDelegateProfile(profile, facetedNavigationConfig)));

        c = new OverrideProfilesConfigCollection(c, profiles);
        transaction.getQuestion().setCollection(c);
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
     * Creates a facet definition for missing metadata
     */
    private FacetDefinition createMissingMetadataFacetDefinition(String field) {
        List<CategoryDefinition> categoryDefinitions = new ArrayList<CategoryDefinition>();
        MissingMetadataFill fill = new MissingMetadataFill();
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
