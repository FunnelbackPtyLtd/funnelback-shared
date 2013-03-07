package com.funnelback.publicui.api;

import lombok.Getter;

/**
 * Builds a {@link Search} request.
 */
public class SearchBuilder {

    @Getter private Search search;
    
    public SearchBuilder() throws SearchAPIException {
        search = new Search();
    }

    /**
     * @param query Query terms to search for.
     * @return
     */
    public SearchBuilder withQuery(String query) {
        search.setQuery(query);
        return this;
    }
    
    /**
     * @param uri Search service URI (Scheme + host + port, such
     * as <tt>http://localhost:8080</tt>.
     * @return
     */
    public SearchBuilder onSearchService(String uri) {
        search.setSearchServiceUri(uri);
        return this;
    }
    
    /**
     * @param collectionId ID of the collection to search on.
     * @return
     */
    public SearchBuilder onCollection(String collectionId) {
        search.setCollection(collectionId);
        return this;
    }

    /**
     * @param num Maximum numbers of results to return.
     * @return
     */
    public SearchBuilder withNumRanks(int num) {
        search.setNumRanks(num);
        return this;
    }
        
}
