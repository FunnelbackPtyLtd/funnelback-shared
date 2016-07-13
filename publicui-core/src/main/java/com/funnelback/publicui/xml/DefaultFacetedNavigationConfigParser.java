package com.funnelback.publicui.xml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Component;

import com.funnelback.common.facetednavigation.marshaller.FacetMarshallerJson;
import com.funnelback.common.facetednavigation.marshaller.xml.FacetMarshallerXml;
import com.funnelback.common.facetednavigation.models.Category;
import com.funnelback.common.facetednavigation.models.Facet;
import com.funnelback.common.facetednavigation.models.categories.DateFieldCategory;
import com.funnelback.common.facetednavigation.models.categories.GscopeCategory;
import com.funnelback.common.facetednavigation.models.categories.MetaDataFieldCategory;
import com.funnelback.common.facetednavigation.models.categories.URLCategory;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.DateFieldFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.GScopeItem;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.MetadataFieldFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.URLFill;

/**
 * Parses a faceted navigation configuration using a Stax Stream parser.
 */
@Log4j2
@Component
public class DefaultFacetedNavigationConfigParser implements FacetedNavigationConfigParser {
    
    
    private final FacetMarshallerJson facetMarshallerJson = new FacetMarshallerJson();
    private final FacetMarshallerXml facetMarshallerXml = new FacetMarshallerXml();
    
    @Override
    public Facets parseFacetedNavigationConfiguration(byte[] configuration) throws FacetedNavigationConfigParseException {
        //We have no Idea what the incoming data is lets work it out.
        
        Optional<List<Facet>> optionalFacets = facetMarshallerJson.unMarshal(configuration);
        
        if(!optionalFacets.isPresent()) {
            optionalFacets = facetMarshallerXml.unmarshallFromUnknownLocation(configuration);
        }
        
        
        if(!optionalFacets.isPresent()) {
            throw new FacetedNavigationConfigParseException("Could not parse faceted navigation configuration");
        }
        
        FacetedNavigationQPOptions facetedNavigationQPOptions = new FacetedNavigationQPOptions();
        List<FacetDefinition> facetDefinitions = convert(optionalFacets.orElse(new ArrayList<>()), 
                                                            facetedNavigationQPOptions);
        
        Facets facets = new Facets();
        facets.facetDefinitions = facetDefinitions;
        facets.qpOptions = facetedNavigationQPOptions.getQPOptions();
        
        this.validate(facets);
        
        return facets;
    }
    
    
    protected List<FacetDefinition> convert(List<Facet> facetsToConvert, 
                                            FacetedNavigationQPOptions facetedNavigationQPOptions) {
        
        List<FacetDefinition> facetDefinitions = new ArrayList<>();
        for(Facet facet : facetsToConvert) {
            
            List<CategoryDefinition> categoryDefinitions = facet.getCategories()
                .stream()
                .map(c -> convert(facet.getName(), c, facetedNavigationQPOptions))
                .collect(Collectors.toList());
            
            FacetDefinition facetDefinition = new FacetDefinition(facet.getName(), categoryDefinitions);
            
            facetDefinitions.add(facetDefinition);
        }
        
        return facetDefinitions;
        
    }
    
    protected CategoryDefinition convert(String facetName,
                                            Category categories, 
                                            FacetedNavigationQPOptions facetedNavigationQPOptions) {
        List<CategoryDefinition> subCategories = new ArrayList<>();
        for(Category subCat : categories.getSubCategories()) {
            subCategories.add(convert(facetName, subCat, facetedNavigationQPOptions));
        }
        
        CategoryDefinition categoryDefinition;
        if(categories instanceof DateFieldCategory) {
            DateFieldFill dateFieldFill = new DateFieldFill(((DateFieldCategory) categories).getMetadataField());
            facetedNavigationQPOptions.add(dateFieldFill);
            categoryDefinition = dateFieldFill;
        } else if(categories instanceof GscopeCategory) {
            GScopeItem gScopeItem = new GScopeItem(((GscopeCategory) categories).getCetegoryName(), ((GscopeCategory) categories).getGscope());
            facetedNavigationQPOptions.add(gScopeItem);
            categoryDefinition = gScopeItem;
        } else if(categories instanceof MetaDataFieldCategory) {
             MetadataFieldFill metadataFieldFill = new MetadataFieldFill(((MetaDataFieldCategory) categories).getMetadataField());
            facetedNavigationQPOptions.add(metadataFieldFill);
            categoryDefinition = metadataFieldFill;
        } else if(categories instanceof URLCategory) {
            URLFill urlFill = new URLFill(((URLCategory) categories).getURIPrefix());
            facetedNavigationQPOptions.add(urlFill);
            categoryDefinition = urlFill;
        } else {
            throw new IllegalStateException("Unknown category definition " 
                                                + categories.getClass().getCanonicalName() 
                                                + " " 
                                                + categories.toString());
        }
        
        categoryDefinition.getSubCategories().addAll(subCategories);
        categoryDefinition.setFacetName(facetName);
        return categoryDefinition;
    }
    
    /**
     * Validates a faceted navigation configuration
     * @param facets
     * @throws FacetedNabigationParseException 
     */
    private void validate(Facets facets) throws FacetedNavigationConfigParseException {
        Set<String> uniqueNames = new HashSet<String>();
        for(FacetDefinition fd: facets.facetDefinitions) {
            if (uniqueNames.contains(fd.getName())) {
                throw new FacetedNavigationConfigParseException("More than one facet with name '"+fd.getName()+"'. Each name must be unique.");
            } else {
                uniqueNames.add(fd.getName());
            }
        }
    }

}
