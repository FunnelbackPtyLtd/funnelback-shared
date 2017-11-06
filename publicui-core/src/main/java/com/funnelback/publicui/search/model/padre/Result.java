package com.funnelback.publicui.search.model.padre;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
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
public class Result implements ResultType {

    /**
     * Date format used to format a date when serialising
     * a result object.
     */
    public static final String DATE_PATTERN_OUT = "d MMM yyyy";

    /**
     * Date formats used to parse dates in PADRE results.
     */
    public static final String[] DATE_PATTERNS_IN = {DATE_PATTERN_OUT, "MMM yyyy", "yyyy"};

    /**
     * String returned by PADRE when a result has no date.
     */
    public static final String NO_DATE = "No Date";

    /**
     * Prefix for Metadata fields in the MetaData Map.
     */
    public static final String METADATA_PREFIX = Schema.METADATA + "_";
    
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
     * <p>Map containing the metadata fields of the results.</p>
     * 
     * <p>The key is the letter the metadata is mapped on.</p>
     * 
     * @see <code>metamap.cfg</code>, <code>xml.cfg</code>
     */
    @Getter private final Map<String, String> metaData = new HashMap<String, String>();
    
    /**
     * <p>Tags associated with a result.</p>
     * 
     * <p>See the <tt>url_tagger</tt> program.</p>
     */
    @Getter private final List<String> tags = new ArrayList<String>();
    
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
    @Getter private final Map<String, Object> customData = new HashMap<String, Object>();
    
    @Getter @Setter private boolean documentVisibleToUser = true;
    
    /**
     * Set true if the URL was promoted using -promote_urls.
     * 
     * @since 15.12
     */
    @Getter @Setter private boolean promoted = false;
    
    /**
     * Set true if the URL was down weighted by result diversification.
     * 
     * This might happen from same site suppression (SSS).
     * 
     * @since 15.12
     */
    @Getter @Setter private boolean diversified = false;
    
    /** Constants for the PADRE XML result packet tags. */
    public static final class Schema {
        
        // CHECKSTYLE:OFF
        public static final String RESULT = "result";
        
        public static final String RANK = "rank";
        public static final String SCORE = "score";
        public static final String TITLE = "title";
        public static final String COLLECTION = "collection";
        public static final String COMPONENT = "component";
        public static final String COLLAPSED = "collapsed";
        public static final String LIVE_URL = "live_url";
        public static final String SUMMARY = "summary";
        public static final String CACHE_URL = "cache_url";        
        public static final String DATE = "date";
        public static final String FILESIZE = "filesize";
        public static final String FILETYPE = "filetype";
        public static final String TIER = "tier";
        public static final String DOCNUM = "docnum";
        public static final String EXPLORE_LINK = "explore_link";
        public static final String KM_FROM_ORIGIN = "km_from_origin";
        public static final String EXPLAIN = "explain";
        public static final String METADATA = "md";
        public static final String TAGS = "tags";
        public static final String RQ = "rq";
        public static final String ATTR_METADATA_F = "f";
        public static final String GSCOPES_SET = "gscopes_set";
        
        public static final String COLLAPSED_SIG = "sig";
        public static final String COLLAPSED_COL = "col";
        public static final String COLLAPSED_COUNT = "count";
        
        public static final String DOCUMENT_VISIBLE_TO_USER = "documentVisibleToUser";
        
        public static final String PROMOTED = "promoted";
        public static final String DIVERSIFIED = "diversified";
        
        // CHECKSTYLE:ON
    }
}

