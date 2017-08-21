package com.funnelback.publicui.xml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.funnelback.common.facetednavigation.marshaller.FacetMarshallerJson;
import com.funnelback.common.facetednavigation.marshaller.xml.FacetMarshallerXml;
import com.funnelback.common.facetednavigation.models.Category;
import com.funnelback.common.facetednavigation.models.Facet;
import com.funnelback.common.facetednavigation.models.categories.AllDocumentsCategory;
import com.funnelback.common.facetednavigation.models.categories.CollectionCategory;
import com.funnelback.common.facetednavigation.models.categories.DateFieldCategory;
import com.funnelback.common.facetednavigation.models.categories.GscopeCategory;
import com.funnelback.common.facetednavigation.models.categories.MetaDataFieldCategory;
import com.funnelback.common.facetednavigation.models.categories.URLCategory;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.AllDocumentsFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.CollectionFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.DateFieldFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.GScopeItem;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.MetadataFieldFill;
import com.funnelback.publicui.search.model.collection.facetednavigation.impl.URLFill;

import lombok.extern.log4j.Log4j2;

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
        
        List<FacetDefinition> facetDefinitions = convert(optionalFacets.orElse(new ArrayList<>()));
        
        Facets facets = new Facets();
        facets.facetDefinitions = facetDefinitions;
        
        this.validate(facets);
        
        return facets;
    }
    
    /**
     * Converts a list of simple data only Facet objects, into their more complicated Facted Navigation counterpart.
     * 
     *  <p>The Facet coming in are the objects returned from the facet marshaller, while the 
     *  FacetDefinitions coming out are ones which contain logic for providing Faceted navigation
     *  within the Public UI.</p>
     * @param facetsToConvert The simple data Facet objects.
     * @return
     */
    protected List<FacetDefinition> convert(List<Facet> facetsToConvert) {
        
        List<FacetDefinition> facetDefinitions = new ArrayList<>();
        for(Facet facet : facetsToConvert) {
            
            List<CategoryDefinition> categoryDefinitions = facet.getCategories()
                .stream()
                .map(c -> convert(facet.getName(), c))
                .collect(Collectors.toList());
            
            FacetDefinition facetDefinition = new FacetDefinition(facet.getName(), categoryDefinitions,
                facet.getSelectionType(),
                facet.getConstraintJoin(),
                facet.getFacetValues(),
                facet.getOrder());
            
            facetDefinitions.add(facetDefinition);
        }
        
        return facetDefinitions;
        
    }
    
    /**
     * Convert a single simple data Category object into its Public UI counterpart which implements the logic 
     * required for Faceted navigation.
     * 
     * @param facetName
     * @param category The data only Category which comes from the Facet marshaler.
     * @param facetedNavigationQPOptions
     * @return The category which implements logic required for Faceted Navigation.
     */
    protected CategoryDefinition convert(String facetName,
                                            Category category) {
        List<CategoryDefinition> subCategories = new ArrayList<>();
        for(Category subCat : category.getSubCategories()) {
            subCategories.add(convert(facetName, subCat));
        }
        
        CategoryDefinition categoryDefinition;
        if(category instanceof DateFieldCategory) {
            DateFieldFill dateFieldFill = new DateFieldFill(((DateFieldCategory) category).getMetadataField());
            categoryDefinition = dateFieldFill;
        } else if(category instanceof GscopeCategory) {
            GScopeItem gScopeItem = new GScopeItem(((GscopeCategory) category).getCategoryName(), ((GscopeCategory) category).getGscope());
            categoryDefinition = gScopeItem;
        } else if(category instanceof MetaDataFieldCategory) {
             MetadataFieldFill metadataFieldFill = new MetadataFieldFill(((MetaDataFieldCategory) category).getMetadataField());
            categoryDefinition = metadataFieldFill;
        } else if(category instanceof URLCategory) {    
            URLFill urlFill = new URLFill(((URLCategory) category).getUriPrefix());
            categoryDefinition = urlFill;
        } else if(category instanceof AllDocumentsCategory) {
            AllDocumentsFill allDocsFill = new AllDocumentsFill(((AllDocumentsCategory) category).getLabel());
            categoryDefinition = allDocsFill;
        } else if(category instanceof CollectionCategory) {
            CollectionCategory cat = (CollectionCategory) category;
            CollectionFill collectionFill = new CollectionFill(cat.getCategoryName(), cat.getCollections());
            categoryDefinition = collectionFill;
        } else {
            //Typically means that the marshaler now produces a Category that we don't understand
            //either thise code needs to map the unknown category to an existing CategoryDefinition. 
            //This might happen if you redefine a Catgegory in the marshaler. Otherwise if you have 
            //created a new Category you will need to create a CategoryDefinition which implements
            //the logic for your new Category
            throw new IllegalStateException("Unknown category definition " 
                                                + category.getClass().getCanonicalName() 
                                                + " " 
                                                + category.toString());
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
