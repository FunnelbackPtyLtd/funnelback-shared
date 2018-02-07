package com.funnelback.publicui.search.model.transaction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.funnelback.publicui.search.model.curator.Curator;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.ContentOptimiserModel;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>This class contains all the output data related to a search.</p>
 * 
 * <p>Most of this data is coming from PADRE, but there is also data
 * computed from the PADRE result set, such as facets.</p>
 *
 * @since 11.0
 */
@ToString
@RequiredArgsConstructor
@JsonIgnoreProperties({"rawPacket","translations"})
public class SearchResponse {

    /** The result packet coming from PADRE */
    @Getter @Setter private ResultPacket resultPacket;
    
    /**
     * <p>Raw XML packet as return by PADRE, for
     * debugging purposes.</p>
     * 
     * <p><b>Note:</b> This is not included in the JSON / XML output
     * for performance reasons</p>
     */
    @XStreamOmitField
    @Getter @Setter private String rawPacket;
    
    /** PADRE return code (0 = success) */
    @Getter @Setter private int returnCode;
    
    /** 
     * Amount of output produced by PADRE responding to the query, in bytes.
     * 
     * @since 15.12
     */
    @Getter @Setter private Integer untruncatedPadreOutputSize;
    
    /**
     * Computed facets based on the PADRE
     * result packet and collection configuration.
     **/
    @Getter private final List<Facet> facets = new ArrayList<>();
    
    /**
     * Returns the first facet with the given name
     * 
     * @since 15.14
     * @param name The name of the facet that is wanted.
     * @return the facet with the given name if not found null is returned.
     */
    @JsonIgnoreProperties
    public Facet getFacetByName(String name) {
        return facets.stream().filter(f -> f.getName().equals(name)).findFirst().orElse(null);
    }
    
    /**
     * Information about facets that is not available from {@link SearchResponse#facets}
     * 
     * @since 15.12
     */
    @Getter private final FacetExtras facetExtras = new FacetExtras();
    
    /**
     * Custom data placeholder allowing any arbitrary data to be
     * stored by hook scripts.
     */
    @Getter private final Map<String, Object> customData = new HashMap<>();
    
    /**
     * Content Optimiser: URL comparison data.
     */
    @Getter @Setter private ContentOptimiserModel optimiserModel;
    
    /**
     * Curator: Curated result packet data data.
     * 
     * @since 13.0
     */
    @Getter @Setter private Curator curator = new Curator();
    
    /**
     * <p>Contains translation messages for the UI, for the locale
     * given in the {@link SearchQuestion}.</p>
     * 
     * <p><b>Note:</b> This is not included in the JSON / XML output
     * for performance reasons</p>

     * @since 12.0
     */
    @XStreamOmitField
    @Getter
    private final Map<String, String> translations = new HashMap<>();
        
    /**
     * Performance metrics of each phase of the transaction lifecycle
     * 
     * @since 12.4
     */
    @Getter @Setter private org.springframework.util.StopWatch performanceMetrics;
    
    /**
     * @return true if the {@link #resultPacket} is not null.
     */
    public boolean hasResultPacket() { return resultPacket != null; }
}
