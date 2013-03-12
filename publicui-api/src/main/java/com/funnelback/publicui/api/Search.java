package com.funnelback.publicui.api;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.web.client.RestTemplate;

import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.xml.SearchXStreamMarshaller;

/**
 * Main class to perform a search against a Funnelback
 * search service.
 */
public class Search {

    private static final String SEARCH_XML_CONTROLLER = "/s/search.xml";
    private static final String SEARCH_URI_TEMPLATE = "?collection={collection}"
        + "&query={query}"
        + "&start_rank={startRank}"
        + "&num_ranks={numRanks}";

    /**
     * Marshaller used to deserialize Funnelback XML.
     */
    @Autowired
    private XStreamMarshaller marshaller;
    
    /**
     * Uri of the search service (Protocol + host + port).
     * Ex: <tt>http://localhost:8080/
     */
    @Setter private String searchServiceUri;
    
    /**
     * Query terms.
     */
    @Setter private String query;
    
    /**
     * Collection to search.
     */
    @Setter private String collection;
    
    /**
     * Offset of the starting result.
     */
    @Setter private int startRank = 1;
    
    /**
     * Number of results to return.
     */
    @Setter private int numRanks = 10;
    
    /**
     * Initialise a new search request.
     * @throws SearchAPIException If an error occurs during the initialisation.
     */
    public Search() throws SearchAPIException {
        this.marshaller = new SearchXStreamMarshaller();
        try {
            marshaller.afterPropertiesSet();
        } catch (Exception e) {
            throw new SearchAPIException(e);
        }
    }
    
    /**
     * Submits a search request to the Funnelback service
     * @return A {@link SearchTransaction}
     */
    public SearchTransaction submit() {
        RestTemplate tpl = new RestTemplate();
        tpl.getMessageConverters().add(new MarshallingHttpMessageConverter(marshaller));
            
        return tpl.getForObject(
                searchServiceUri+SEARCH_XML_CONTROLLER+SEARCH_URI_TEMPLATE, SearchTransaction.class,
                collection, query, startRank, numRanks);
    }
    
}
