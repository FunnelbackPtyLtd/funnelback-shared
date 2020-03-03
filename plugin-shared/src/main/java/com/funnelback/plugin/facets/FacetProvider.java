package com.funnelback.plugin.facets;

import com.funnelback.plugin.index.IndexConfigProviderContext;

/**
 * Provide extra faceted navigation to use during query processing.
 * 
 *
 */
public interface FacetProvider {

    /**
     * Supply additional faceted navigation configuration.
     * 
     * Additional faceted navigation is supplied as a JSON similar to the API
     * <code>
     * GET /faceted-navigation/v2/collections/{collection}/profiles/{profile}/facet/{id}/views/{view}
     * <code>
     * 
     * This expects to return a list <code>[]</code> of Facets. The id,
     * lastModified and created fields do not need to be set.
     * 
     * Since the acceptable combinations of facets is complicated, it may be easier 
     * to design the facets you want using the faceted navigation UI. After configuring 
     * your facet, extract the facet configuration JSON from the preview screen and omit the 
     * id, lastModified and created fields.
     * 
     * For example:
     * <code>
     * [{
     * "name": "Authors",
     * "facetValues": "FROM_SCOPED_QUERY_HIDE_UNSELECTED_PARENT_VALUES",
     * "constraintJoin": "AND",
     * "selectionType": "SINGLE",
     * "categories": [{ "type": "MetaDataFieldCategory", "subCategories": [], "metadataField": "author" }],
     * "order": [ "SELECTED_FIRST", "COUNT_DESCENDING"]
     * }]
     * </code>
     * 
     * During query processing each facet must have a unique name. Facets defined on the profile
     * will be used in preference to facets defined from the plugin. After that facets from plugins will
     * be used so long as the facet, by name, does not already exist.
     * 
     * @param context Context passed to the plugin when called, will be called with a profile set.
     * @return JSON as a string which contains a list of facet definitions as returned by the
     * API. 
     */
    public default String extraFacetedNavigation(IndexConfigProviderContext context) {
        return null;
    }
    
}
