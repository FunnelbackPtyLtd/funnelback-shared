package com.funnelback.publicui.search.model.padre;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.funnelback.publicui.search.model.related.RelatedDocument;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder.ListMultimapBuilder;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A single search result.
 * 
 * @since 11.0
 */
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Result implements ResultType {
    
    /** Rank of the result (From 1 to n) */
    @Getter @Setter private Integer rank;
    /** Score of the result (From 1000 to 0) */
    @Getter @Setter private Integer score;
    /** Title of the result */
    @Getter @Setter private String title;
    
    /**
     * <p>ID of the collection to which this result belongs.</p>
     * 
     * <p>This is usually the same ID as the collection being
     * searched, except for Meta collections where results can
     * come from different sub-collections.</p>
     */
    @Getter @Setter private String collection;
    
    /**
     * <p>For meta collections it's the internal component
     * number of the sub-collection this result is coming
     * from.</p>
     * 
     * <p>For non-meta collections it's always zero.</p>
     * 
     * @see Result#collection
     */
    @Getter @Setter private Integer component;
    
    /**
     * <p>Result collapsing information for this result.</p>
     * 
     * <p>Will be null if no results are collapsed with this
     * one.</p>
     * 
     * @since 12.5
     */
    @Getter @Setter private Collapsed collapsed;
    
    /**
     * <p>URL to access the search result.</p>
     * 
     * <p>It's identical to the {@link #indexUrl} initially, but might
     * have been transformed by a hook script. The click tracking URL
     * will be built based on this URL, so if you need to modify the URL
     * that's recorded in the click log, this field should be changed.</p>
     * 
     * <p>In the default form, its only use it to display a proper URL
     * when the user mouse-over the result title link.</p>
     **/
    @Getter @Setter private String liveUrl;
    
    /** Query-biased summary */
    @Getter @Setter private String summary;
    
    /**
     * All texts available for generating summary text
     * 
     * @since 15.18 
     */
    @Getter @Setter private String allSummaryText;
    
    /** URL to access the cached version of the result. */
    @Getter @Setter private String cacheUrl;
    
    /** Date of the search result */
    @Getter @Setter private Date date;
    
    /**
     * Size of the file corresponding to the search
     * results, in bytes.
     */
    @Getter @Setter private Integer fileSize;
    
    /**
     * File type of the result, usually the file extension
     * ("pdf", "xls", "html", ...)
     */
    @Getter @Setter private String fileType;
    
    /**
     * Tier number to which this search results belongs.
     * 
     * @see ResultPacket#getResultsWithTierBars()
     */
    @Getter @Setter private Integer tier;
    
    /**
     * Internal document number of the result
     * in the index.
     */
    @Getter @Setter private Integer docNum;
    
    /**
     * Link to the <em>also of interest</em> CGI.
     */
    @Getter @Setter private String exploreLink;
    
    /**
     * Distance in kilometres of this search result
     * from the origin set when the query is run.
     */
    @Getter @Setter private Float kmFromOrigin;
    
    /**
     * <p>Map containing the separators defined for each metadata class.</p>
     *
     * <p>Intended only for internal use, and not exposed in the XML/JSON data model.</p>
     * 
     * <p>Please note that currently separators are defined globally, however this data-model
     *    aims to support the possibility of per-class separators in the future.</p>
     * 
     * @see <code>Metadata classes</code>
     * @since 15.16
     */
    @JsonIgnore
    @XStreamOmitField
    @Getter private final ListMultimap<String, String> definedMetadataSeparators = ListMultimapBuilder.hashKeys().arrayListValues().build();

    /**
     * <p>Multi-Map containing the list of metadata values for each metadata fields for each result.</p>
     * 
     * <p>The key is the metadata class name as defined in the metadata mappings.</p>
     *
     * <p>The values are each mapped metadata value, split based on any defined separators.</p>
     * 
     * @see <code>Metadata classes</code>
     * @since 15.16
     */
    @Getter private final ListMultimap<String, String> listMetadata = ListMultimapBuilder.hashKeys().arrayListValues().build();

    /**
     * <p>Multi-Map containing the separators originally used by the metadata values within in newMetadata.</p>
     *
     * <p>Intended only for internal use, and not exposed in the XML/JSON data model.</p>
     * 
     * <p>This map is only of interest if the specific separators are meaningful to an implementation
     *    which is hopefully uncommon. If values are added to the newMetadata map, new separators
     *    may be added here also. Some separator from the definedMetadataSeparators list will be used
     *    for any added metadata if this map lacks sufficient values.</p>
     * 
     *
     * @see <code>Metadata classes</code>
     * @since 15.16
     */
    @JsonIgnore
    @XStreamOmitField
    @Getter private final ListMultimap<String, String> listMetadataSeparators = ListMultimapBuilder.hashKeys().arrayListValues().build();

    /**
     * <p>Tags associated with a result.</p>
     * 
     * <p>See the <tt>url_tagger</tt> program.</p>
     */
    @Getter private final List<String> tags = new ArrayList<>();
    
    /** Quick links associated with the result. */
    @Getter @Setter private QuickLinks quickLinks;
    
    /**
     * <p>URL to display for the result.</p>
     * 
     * <p>Initially identical to {@link #indexUrl} and {@link #liveUrl},
     * but might have been transformed by a hook script. This URL
     * can be used to display a different URL from the actual one, while
     * preserving the {@link #liveUrl} for the user to access the result.</p>
     * 
     * <p>In the default form, this URL is displayed in the <code>&lt;cite&gt;</code>
     * block for the the result</p>
     **/
    @Getter @Setter private String displayUrl;
        
    /** URL for click tracking. */
    @Getter @Setter private String clickTrackingUrl;

    /**
     * Explain data used in the Content Optimiser.
     */
    @Getter @Setter private Explain explain;
    
    /**
     * Original URL from the index, taken from indexUrl before any transformation.
     */
    @Getter @Setter private String indexUrl;

    /**
     * Set of GScope Numbers that this result has.
     */
    @Getter @Setter private Set<String> gscopesSet;

    /**
     * Custom data placeholder allowing any arbitrary data to be
     * stored by hook scripts.
     */
    @Getter private final Map<String, Object> customData = new HashMap<>();

    /**
     * Documents related to this result which have been fetched by RelatedDocumentFetcher.
     * 
     * The map key is chosen by the related document fetching configuration and the
     * set of related documents are those that were fetched based on the metadata
     * relationships traversed by the configuration.
     *
     * @since 15.16
     */
    @Getter private final Map<String, Set<RelatedDocument>> relatedDocuments = new HashMap<>();

    @Builder.Default @Getter @Setter private boolean documentVisibleToUser = true;
    
    /**
     * Set true if the URL was promoted using -promote_urls.
     * 
     * @since 15.12
     */
    @Builder.Default @Getter @Setter private boolean promoted = false;
    
    /**
     * Set true if the URL was down weighted by result diversification.
     * 
     * This might happen from same site suppression (SSS).
     * 
     * @since 15.12
     */
    @Builder.Default @Getter @Setter private boolean diversified = false;
}

