package com.funnelback.publicui.test.contentauditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.funnelback.publicui.contentauditor.CountThresholdMetadataFieldFill;
import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.search.model.collection.facetednavigation.CategoryDefinition;
import com.funnelback.publicui.search.model.collection.facetednavigation.FacetDefinition;

public class FacetedNavigationCountThresholdMetadataFieldFillTestUtils {

    /** 
     * Builds a facet config matching faceted-navigation-metadatafieldfill's
     * but with CountThresholdMetadataFieldFill instead of straight MetadataFieldFill
     * category definitions.
     */
    public static FacetedNavigationConfig buildFacetConfig() {

        List<FacetDefinition> facetDefinitions = new ArrayList<FacetDefinition>();
        
        List<CategoryDefinition> categories = 
            metadataFillWithThresholdDefition("Z", "Location", 0, 
                metadataFillWithThresholdDefition("Y", "Location", 0, 
                    metadataFillWithThresholdDefition("X", "Location", 0, new ArrayList<>())
                )
            );
        categories.addAll(metadataFillWithThresholdDefition("O", "Location", 0, new ArrayList<>()));
        facetDefinitions.add(new FacetDefinition("Location", categories));
        
        facetDefinitions.add(new FacetDefinition("Job Category",  
            metadataFillWithThresholdDefition("W", "Job Category", 0, 
                metadataFillWithThresholdDefition("V", "Job Category", 0, new ArrayList<>())
            )
        ));

        facetDefinitions.add(new FacetDefinition("Type",  
            metadataFillWithThresholdDefition("U", "Type", 0, new ArrayList<>())
        ));
        
        FacetedNavigationConfig facetedNavigationConfig = new FacetedNavigationConfig("-rmcf=ZWXYUV", facetDefinitions);
        return facetedNavigationConfig;
    }
    
    /** Convenience method to build a CountThresholdMetadataFieldFill with the given settings. */
    private static List<CategoryDefinition> metadataFillWithThresholdDefition(String metadataClass, String facetName, int threshold, List<CategoryDefinition> subCategory) {
        CountThresholdMetadataFieldFill result = new CountThresholdMetadataFieldFill(metadataClass, 1);
        result.setFacetName(facetName);
        result.getSubCategories().clear();
        result.getSubCategories().addAll(subCategory);
        
        return new ArrayList<>(Arrays.asList(new CategoryDefinition[]{result}));
        // Why wrap again with ArrayList? Because otherwise addAll throws an Unsupported exception - weird
    }
}
