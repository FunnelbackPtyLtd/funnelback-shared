package com.funnelback.publicui.search.model.padre;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.funnelback.publicui.search.model.util.map.AutoConvertingMap;
import com.funnelback.publicui.search.model.util.map.Converters;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A PADRE result packet, containing search results.
 * 
 * @since 11.0
 */
@NoArgsConstructor
public class ResultPacket {

    /** Details about the index and PADRE version. */
    @Getter @Setter private Details details;
    
    /** Original query terms */
    @Getter @Setter private String query;


    /**
     * <p>Query terms as processed by PADRE.</p>
     * 
     * <p>This could be different from the {@link #query} if the
     * initial {@link #query} contained character that PADRE ignores
     * of if a query transformation was applied by PADRE.</p>
     */
    @Getter @Setter private String queryAsProcessed;

    /**
     * <p>Query terms as provided to PADRE by the user.</p>
     * 
     * <p>The query at this point has been encoded to UTF8, possibly
     * there may have been percent decoding if query processor 
     * option <code>udcq</code> is used. The value is otherwise 
     * unmodified.</p>
     */
    @Getter @Setter private String queryRaw;
 
    /**
     * <p>System-generated query terms as provided to PADRE.</p>
     * 
     * <p>The query separately provided by the system
     * using the processor option or CGI parameter <code>s</code>.
     * This value has been UTF8 encoded, otherwise it is unchanged.
     * It is ultimately preprocessed and concatenated to the 
     * preprocessed user query and parsed.</p>
     */
    @Getter @Setter private String querySystemRaw;

    /**
     * The query, cleaned from any operator or constraint that was
     * automatically added by the faceted navigation system.
     */
    @Getter @Setter private String queryCleaned;
    
    /** ID of the collection being searched. */
    @Getter @Setter private String collection;
    
    /**
     * <p>List of additional queries for the Query Blending system.</p>
     * 
     * <p><strong>Warning:</strong> When accessing this field from a template
     * or a hook script you must use the following syntax: <code>QSups</code>,
     * instead of <code>qSups</code> due to the Javabeans naming conventions.</p>
     * 
     * @see "blending.cfg"
     */
    @Getter private final List<QSup> qSups = new ArrayList<>();
    
    /**
     * Summary counts and data about the results (How many documents
     * matched, which page is currently returned, etc.).
     */
    @Getter @Setter private ResultsSummary resultsSummary = new ResultsSummary();
    
    /** Spelling suggestions. */
    @Getter @Setter private Spell spell;
    
    /** List of results. */
    @Getter @Setter private List<Result> results = new ArrayList<>();
    
    /** List of tier bars */
    @Getter final private List<TierBar> tierBars = new ArrayList<>();
    
    /** Error occurred during the search, if any. */
    @Getter @Setter private Error error;
    
    /** Time spent by PADRE processing the query, in milliseconds */
    @Getter @Setter private Integer padreElapsedTime;

    /**
     * Time spent by PADRE processing contextual navigation,
     * in seconds.
     */
    @Getter @Setter private Float phlusterElapsedTime;
    
    /**
     * Indicates how the query was internally processed
     * by PADRE.
     */
    @Getter @Setter private String queryProcessorCodes;

    /** Contextual navigation suggestions. */
    @Getter @Setter private ContextualNavigation contextualNavigation;
    
    /**
     * <p>Metadata counts (Used in faceted navigation).</p>
     * 
     * <p>The key is the tuple <code>metadata_class:value</code> and the
     * value is the count.</p>
     * 
     * <p>
     *     Examples:</p>
     *  <ul>
     *      <li>a:mozart =&gt; 12</li>
     *      <li>a:beethoven =&gt; 6</li>
     *      <li>-a: =&gt; 10 (i.e. items with no a metadata)</li>
     *  </ul>
     */
    @Getter private final Map<String, Integer> rmcs = new HashMap<>();
    
    /**
     * <p>Metadata counts (Used in faceted navigation) including top n results
     * for each count.</p>
     * 
     * <p>The key is the couple of <code>metadata_class:value</code>. The value is a list of
     * the first n results that would be returned if the metadata constraint was applied.</p>
     * 
     * <p>The key is the same one as the {@link ResultPacket#rmcs} map.</p>
     * 
     * <p>Examples:</p>
     *     <ul>
     *         <li>a:shakespeare =&gt;
     *             <ul>
     *                 <li>Romeo and Juliet</li>
     *                 <li>Cleopatra</li>
     *                 <li>...</li>
     *             </ul>
     *         </li>
     * 
     *         <li>a:voltaire =&gt;
     *             <ul>
     *                 <li>Zadig</li>
     *                 <li>...</li>
     *             </ul>
     *         </li>
     *     </ul>
     * 
     * @since 11.2
     */
    @Getter private final Map<String, List<RMCItemResult>> rmcItemResults = new HashMap<>();
    
    /**
     * <p>Metadata ranges.</p>
     * 
     * <p>The key is the <code>metadata_class</code> and the
     * value is a DoubleRange object with maximum and minimum values.</p>
     * 
     * <p>
     *     Example:</p>
     *  <ul>
     *      <li>P =&gt; Range(100.0, 400.0)</li>
     *  </ul>
     */
    @Getter private final Map<String, Range> metadataRanges = new HashMap<>();
    
    /**
     * <p>Bounding boxes for Geospatial metadata classes</p>
     * 
     * <p>The key is the <code>metadata_class</code> and the value is
     * the bounding box for the coordinates for that class that appeared
     * in the results (up to the DAAT limit).</p>
     * 
     */
    @Getter public Map<String, GeoBoundingBox> boundingBoxes = new HashMap<>();
    
    /**
     * <p>URL counts (Used in faceted navigation).</p>
     * 
     * <p>The key is the URL itself and the value is the count.
     * If the URL starts with <code>http://</code>, it's omitted.</p>
     * 
     * <p>Examples:</p>
     *      <ul>
     *          <li>www.example.com/about =&gt; 12</li>
     *          <li>www.example.com/contact =&gt; 6 </li>
     *          <li>https://secure.example.com/login =&gt; 5</li>
     *      </ul>
     */
    @Getter private final Map<String, Integer> urlCounts = new HashMap<>();

    /**
     * <p>GScope counts (Used in faceted navigation)</p>
     * 
     * <p>The key is the GScope number and the value is the count.</p>
     * 
      * <p><strong>Warning:</strong> When accessing this field from a template
     * or a hook script you must use the following syntax: <code>GScopeCounts</code>,
     * instead of <code>gScopeCounts</code> due to the Javabeans naming conventions.</p>
     */
    @Getter private final Map<String, Integer> gScopeCounts = new AutoConvertingMap<>(Converters.INTEGER_TO_STRING, "gScopeCounts", new HashMap<>()); 
    
    /**
     * <p>Date counts (Used in faceted navigation)</p>
     * 
     * <p>The key is a tuple <code>metadata_class:value</code>, with the value
     * being a year or a label, e.g. <code>d:2003</code> or <code>d:Yesterday</code>.</p>
     * 
     * @since 12.0
     */
    @Getter private final Map<String, DateCount> dateCounts = new HashMap<>();
    
    /**
     * <p>Regular expression to use to highlight query terms in titles,
     * summaries or metadata.</p>
     * 
     * <p>PADRE provides the regular expression to use depending on the
     * query terms and other factors.</p>
     */
    @Getter @Setter private String queryHighlightRegex;
    
    /**
     * <p>Origin of the search, for geographical searches.</p>
     * 
     * <p>The first slot contains the latitude, the second slot
     * contains the longitude.</p>
     */
    @Getter @Setter private Float[] origin = new Float[0]; 
    
    /**
     * <p>List of prominent entities.</p>
     * 
     * <p>See the experimental <code>-fluent</code> query processor option.</p>
     */
    @Getter private final Map<String, Integer> entityList = new HashMap<>();
    
    /**
     * Scopes (URL prefixes, not Gscope) included via the <code>scope</code> 
     * query processor option.
     */
    @Getter private final List<String> includeScopes = new ArrayList<>();
    
    /**
     * Scopes (URL prefixes, not Gscopes) excluded via the <code>scope</code>
     * query processor option.
     */
    @Getter private final List<String> excludeScopes = new ArrayList<>();
    
    /**
     * A {@link Map} of floats that describe the cooler ranking weights. Weights are 
     * identified by the cooler variable short name + id, and the map is only populated when explain mode is on.
     */
    @Getter private final Map<CoolerWeighting, Float> coolerWeights = new HashMap<>();

    /**
     * <p>A {@link Map} of Strings that describes how to calculate the potential improvement for ranking 
     * on each feature when the content optimiser is used.</p>
     *  
     * <p>Ranking features are identified by the cooler variable short name + id, 
     * and the map is only populated when explain mode is on.</p>
     */
    @Getter private final Map<CoolerWeighting, String> explainTypes = new HashMap<>();
    
    /**
     * A {@link List} of stop words used by the query processor. Only populated when explain mode is on.
     */
    @Getter private final List<String> stopWords = new ArrayList<>();
    
    /**
     * A {@link SetMultimap} of Strings that describes the results of stemming on the query. Only populated when explain mode is on.
     * The map keys are content terms, and the value(s) are the query terms that the key matches.   
     */
    @Getter private final SetMultimap<String, String> stemmedEquivs = HashMultimap.create();

    /**
     * A {@link Map} of long names for cooler ranking variables, keyed by the cooler variable short names + id. Only populated when explain mode is on.    
     */
    @Getter private final Map<CoolerWeighting, String> coolerNames = new HashMap<>();
    
    /**
     * <p>Contains SVG data returned by PADRE, for example an SVG representation
     * of the syntax tree.</p>
     * 
     * <p>The value contains the actual SVG XML string which can be directly used
     * in an HTML source for browsers that supports it.</p>
     * 
     * @since 12.0
     */
    @Getter private final Map<String, String> svgs = new HashMap<>();
    
    /**
     * <p>A list containing the count of unique values for a metadata class grouped by
     * another metadata class.</p>
     * 
     * <p>Each element of the list is the result of the count of unique values
     * of metadata 'X' grouped by metadata 'Y'.</p>
     * 
     * @since 15.8
     */
    @Getter private final List<UniqueByGroup> uniqueCountsByGroups = new ArrayList<>();
    
    /**
     * <p>A list containing the sum of a numeric metadata class grouped by
     * another metadata class</p>
     *  
     * <p>Each element of the list is the result of the sum of metadata 'X'
     * grouped by metadata 'Y'</p>
     * 
     */
    @Getter private final List<SumByGroup> sumByGroups = new ArrayList<>();
    
    /**
     * <p>A list containing the counts of term occurrences in a metadata class.</p>
     * <p>See query processor option -countIndexedTerms.</p>
     * 
     * @since 15.10
     */
    @Getter private final List<IndexedTermCounts> indexedTermCounts = new ArrayList<>();
    
    /**
     * <p>A map containing the number of documents each collection contributed to the result set.</p>
     * <p>The key is the collection name and the value is the number of documents that collection
     * contributed</p>
     * 
     * @since 15.12
     */
    @Getter private final Map<String, Long> documentsPerCollection = new HashMap<>();
    
    /**
     * <p>A map of the numeric metadata to total of that numeric metadata in the result set</p>
     * 
     * <p>The key is the metadata class. The value is the sum.</p>
     */
    @Getter private final Map<String, Double> metadataSums = new HashMap<>();
    
    /**
     * Test if the packet contains results.
     * @return true if the packet contains at least one {@link Result}.
     */
    public boolean hasResults() { return results != null && !results.isEmpty(); }
    
    /**
     * <p>Get the results <em>and</em> the tier bars mixed together.</p>
     * 
     * <p>This is a convenience method if you need to iterate over the result set
     * and display tier bars.</p>
     * 
     * @return A list containing both {@link Result} and {@link TierBar}, in
     * the order returned by PADRE.
     */
    @JsonIgnore
    public List<ResultType> getResultsWithTierBars() {
        if (tierBars != null && !tierBars.isEmpty()) {
            ArrayList<ResultType> out = new ArrayList<>();
            for (TierBar tb: getTierBars()) {
                out.add(tb);
                out.addAll(getResults().subList(tb.getFirstRank(), tb.getLastRank()));
            }
            return out;            
        } else {
            return Collections.unmodifiableList(this.getResults());
        }
    }
}
