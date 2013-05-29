package com.funnelback.publicui.search.model.transaction;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.log.Log;
import com.funnelback.publicui.utils.SingleValueMapWrapper;
import com.maxmind.geoip.Location;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import lombok.*;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.security.Principal;
import java.util.*;
import java.util.regex.Pattern;

/**
 * <p>This class contains all the input data related to a search.</p>
 * 
 * <p>This data will be collected from the input parameters (query string
 * parameters) and possibly transformed by some processing before PADRE
 * is called.</p>
 * 
 * @since 11.0
 */
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties("principal")
public class SearchQuestion {
        
    /**
     * <p>Query terms.</p>
     * 
     * <p>This will be possibly transformed before being passed to
     * PADRE, depending of the configuration.</p>
     */
    @Getter @Setter private String query;
    
    /**
     * Original query, as entered by the user.
     */
    @Getter @Setter private String originalQuery;
    
    /**
     * Searched {@link Collection}.
     */
    @Getter @Setter private Collection collection;
    
    /**
     * Search {@link Profile}, defaulting to "_default"
     */
    @NonNull
    @javax.validation.constraints.Pattern(regexp="[\\w-_]+")
    @Getter @Setter private String profile = DefaultValues.DEFAULT_PROFILE;
    
    /**
     * Specific component of a meta-collection to query
     */
    @Getter @Setter private String[] clive;
    
    /**
     * Display form (template)
     */
    @NonNull
    @javax.validation.constraints.Pattern(regexp="[\\w-_]+")        
    @Getter @Setter private String form = DefaultValues.DEFAULT_FORM;
    
    /**
     * Contextual Navigation: last clicked cluster
     */
    @Getter @Setter private String cnClickedCluster;
    
    /**
     * Contextual Navigation: Previous clicked clusters
     */
    @Getter private final List<String> cnPreviousClusters = new ArrayList<String>();

    /**
     * <p><tt>meta_*</tt> / <tt>query_*</tt> input parameters, transformed in query expressions.</p>
     * 
     * <p>Those parameters are supposed to be specified by the user, usually
     * in an advanced search form. If you need to inject parameters for technical / behind-the-scene
     * reasons, consider using {@link #systemMetaParameters}.</p>
     * 
     * @see #systemMetaParameters
     */
    @Getter final private List<String> metaParameters = new ArrayList<String>();
    
    /**
     * <p><tt>smeta_*</tt> / <tt>squery_*</tt> input parameters, transformed in query expressions.</p>
     * 
     * <p>Those parameters are supposed to be &quot;technical&quot; parameters,
     * usually injected through a hook script. If you want the user to specify parameters,
     * consider using {@link #metaParameters}.</p>
     * 
     * @see #metaParameters
     * @since 12.2
     */
    @Getter final private List<String> systemMetaParameters = new ArrayList<String>();
    
    /**
     * <p>Additional parameters to pass as-is to PADRE.</p>
     * 
     * <p>The values of this map will be passed through to PADRE and won't be processed
     * at all by the Modern UI. Only parameters relevant to PADRE should be injected here.</p>
     * 
     * <p>Depending on the parameters you want to inject or manipulate, consider using
     * {@link #inputParameterMap} or {@link #rawInputParameters}. Those parameters will be
     * processed by the Modern UI (For example to inject faceted navigation constraints).</p> 
     * 
     * <p>Note that this map is populated <strong>before</strong> the first hook script is run: Any
     * additional parameter injected in {@link #inputParameterMap} or {@link #rawInputParameters}
     * in a hook script won't be copied to this map.</p> 
     */
    @Getter final private Map<String, String[]> additionalParameters = new HashMap<String, String[]>();
    
    /**
     * List of environment variables to pass to PADRE.
     */
    @Getter final private Map<String, String> environmentVariables = new HashMap<String, String>();

    /**
     * <p>Dynamic query processor options for PADRE, in addition to the one set in
     * <tt>collection.cfg</tt>.</p>
     * 
     * <p>Will be updated by the input processing.</p>
     */
    @Getter final private List<String> dynamicQueryProcessorOptions = new ArrayList<String>();

    /**
     * <p>User keys for early binding Document Level Security.</p>
     */
    @Getter final private List<String> userKeys = new ArrayList<String>();
    
    /**
     * <p>List of selected facets.</p>
     * 
     * <p>Contains the name of the facets that were selected, such as
     * "Location" or "Brand", but not the actual value.</p>
     */
    @Getter final private Set<String> selectedFacets = new HashSet<String>();
    
    /**
     * <p>List of selected facets categories.</p>
     * 
     * <p>Contains the actual values that were selected, indexed by facet.
     * For example: "Location" => ("Sydney", "Melbourne").</p>
     */
    @Getter final private Map<String, List<String>> selectedCategoryValues = new HashMap<String, List<String>>();
    
    /**
     * Query constraints to apply for faceted navigation
     * (In addition to other query expressions).
     */
    @Getter @Setter private List<String> facetsQueryConstraints = new ArrayList<String>();
    
    /**
     * GScope constraints to apply for faceted navigation
     * (In addition to existing gscope constraints).
     */
    @Getter @Setter private String facetsGScopeConstraints;
    
    /**
     * Whether the request is impersonated (Document Level Security)
     */
    @Getter @Setter private boolean impersonated;
    
    /**
     * Request identifier to log for this transaction. Depending of the collection configuration
     * it can be an IP address, an md5 hash of the address, nothing ("-") or null.
     * 
     *  @since 12.4 - Renamed from <code>userId</code>
     *  @since 12.5 - Renamed from <code>userIdToLog</code>
     */
    @Getter @Setter private String requestId = Log.REQUEST_ID_NOTHING;
    
    /**
     * <p>Raw input parameters</p>
     * 
     * <p>Contains all the input parameters (query string / request parameters).</p>
     * 
     * <p>Be aware that the value type is <code>String array</code>, allowing for multiple
     * value of the same parameter (e.g. <code>&amp;param=value1&amp;param=value2</code>).
     * Putting a single valued <code>String</code> in this Map will not work, it must be
     * an array of size one. Example in a Groovy hook script:
     * <pre>
     * transaction.question.rawInputParameters["param"] = [ "value" ]
     * </pre>
     * </p>
     * 
     * <p>{@link #inputParameterMap} provides a simpler way to inject or retrieve simple mono-valued
     * parameters. {@link #inputParameterMap} and {@link #rawInputParameters} are backed by the same Map: Any
     * change made in one will be reflected in the other.</p>
     * 
     * @see #inputParameterMap
     */
    @Getter private final Map<String, String[]> rawInputParameters = new HashMap<String, String[]>();

    /**
     * <p>Input parameters map.</p>
     * 
     * <p>Contains all the input parameters (query string / request parameters) in a
     * convenient fashion: Only the first value of each parameter is returned to avoid
     * having to deal with arrays of Strings.</p>
     * 
     * <p>For example if the query string is <code>&amp;param=value1&amp;param=value2</code> 
     * then this map will contain only one key-value pair: <tt>param=value1</tt>. The <tt>value2</tt>
     * won't be available unless you use {@link #rawInputParameters}.</p>
     * 
     * <p>{@link #rawInputParameters} provides a way to inject or retrieve multi-valued parameters. Both
     * {@link #inputParameterMap} and {@link #rawInputParameters} are backed by the same Map: Any change made
     * in one will be reflected in the other.</p>
     * 
     * @see #rawInputParameters
     */
    @Getter private final Map<String, String> inputParameterMap = new SingleValueMapWrapper(rawInputParameters);
    
    /**
     * <p>Indicates if this question is part of the "main" search, or part of an "extra"
     * search.</p>
     * 
     * <p>This is needed to perform some actions only on the main search but not for every
     * extra search.</p>
     */
    @Getter @Setter private boolean extraSearch = false;
    
    /**
     * <p>{@link Locale} to use for the search.</p>
     * 
     * <p>This will affect the UI only, to configure the locale
     * for the query processor please refer to the corresponding option
     * <tt>-lang</tt>.</p>
     * 
     * @since 12.0
     */
    @Getter @Setter private Locale locale = Locale.getDefault();

    /**
     * <p>{@link Location} information detected for the remote user based on
     * information from their request.</p>
     * 
     * <p>Properties may include, depending on what can be determined:</p>
     * 
     * <ul>
     *     <li>area__code</li>
     *     <li>city</li>
     *     <li>countryCode</li>
     *     <li>countryName</li>
     *     <li>dma__code</li>
     *     <li>latitude</li>
     *     <li>longitude</li>
     *     <li>metro__code</li>
     *     <li>postalCode</li>
     *     <li>region</li>
     * </ul>
     * 
     * <p>See also: <a href="http://www.jarvana.com/jarvana/view/org/dspace/dependencies/dspace-geoip/1.2.3/dspace-geoip-1.2.3-javadoc.jar!/com/maxmind/geoip/Location.html">
     * MaxMind Location class Javadoc</a></p>
     * 
     * @since 12.4
     */
    @Getter
    @Setter
    private Location location;

    /**
     * <p>Principal representing the remote user for the current request.</p>
     * 
     * <p>Will be NULL if the user isn't authenticated.</p>
     * 
     * @since 12.2
     */
    @XStreamOmitField
    @Getter @Setter private Principal principal;

    /**
     * Common query string parameters names.
     * 
     * @since 11.0
     */
    public static class RequestParameters {
        
        /** Collection ID */
        public static final String COLLECTION = "collection";
        
        /** Query terms */
        public static final String QUERY = "query";
        
        /** Search profile */
        public static final String PROFILE = "profile";
        
        /** Meta components restriction*/
        public static final String CLIVE = "clive";
        
        /** Gscope constraint */
        public static final String GSCOPE1 = "gscope1";
        
        /** Name of the form (template) to use to display results */
        public static final String FORM = "form";
        
        /** Number of results per page */
        public static final String NUM_RANKS = "num_ranks";
        
        /** Explore: Number of query terms */
        public static final String EXP = "exp";

        /**
         * Prefix used on faceted navigation parameters
         * (Ex: <code>f.Location|X=...</code>
         */
        public static final String FACET_PREFIX = "f.";
        
        /**
         * Checkbox used to preserve the facet scope when
         * running a query.
         */
        public static final String FACET_SCOPE = "facetScope";
        
        /**
         * Pattern to use to find faceted navigation parameters.
         */
        public static final Pattern FACET_PARAM_PATTERN = Pattern.compile("^" + FACET_PREFIX.replaceAll("\\.", "\\\\.") + "([^\\|]+)(\\|(.*))?");
        
        /**
         * Content Optimiser: URL of the document to optimise.
         */
        public static final String CONTENT_OPTIMISER_URL = "optimiser_url";
        
        /**
         * Content Optimiser: Advanced mode toggle.
         */
        public static final String CONTENT_OPTIMISER_ADVANCED = "advanced";

        /** Content Optimiser: Activate explain mode on PADRE. */
        public static final String EXPLAIN = "explain";        

        /** Special parameter to redirect directly to the first result */
        public static final String ONESHOT = "oneshot";
        
        /** Encoding of the input parameters (query ...) */
        public static final String ENC = "enc";
        
        /**
         * <p>Parameter to specify the locale of the search query</p>
         * 
         * <p>Will affect the UI as well as the query processor.</p>
         */
        public static final String LANG = "lang";
        
        /** Parameter to specify the locale of the UI only */
        public static final String LANG_UI = "lang.ui";

        /** Parameter specifying the origin point for the search request */
        public static final String ORIGIN = "origin";
        
        /** PADRE debug mode */
        public static final String DEBUG = "debug";

        /**
         * Common request header names
         * 
         * @since 11.1
         */
        public static class Header {
            /**
             * Header used to provide the originating user's IP address, which may differ from
             * the normal remote address if the request is forwarded by a proxy or wrapped by
             * a CMS.
             */
            public static final String X_FORWARDED_FOR = "X-Forwarded-For";        

            /**
             * Header used to provide the server hostname which was requested. Multiple
             * hostnames may be served by a single Funnelback server over a single IP 
             * address.
             */
            public static final String HOST = "host";        

            /**
             * Header used to provide the URL of the web page from which the user accessed
             * the search service.
             */
            public static final String REFERRER = "Referrer";
        }

        /**
         * Common request parameter names used in the cache
         * controller.
         * 
         * @since 11.0
         */
        public static class Cache {
            
            /** URL of the original document */
            public static final String URL = "url";
        }
        
        /**
         * Common request parameter names used in the click
         * controller.
         * 
         * @since 11.0
         */
        public static class Click {
            /**
             * Target URL to redirect to.
             */
            public static final String URL = Cache.URL;
            
            
            /** URL of the target in the index (may be different to the redirect URL).
             * This is the URL that will be logged in clicks.log */
            public static final String INDEX_URL = "index_url";
            
            /** Authorisation token used to prevent spoofed redirects */
            public static final String AUTH = "auth";
            
            /** Search referrer */
            public static final String SEARCH_REFERER = "search_referer";
            
            /** Type of click for logging purposes */
            public static final String TYPE = "type";
            
            /**
             * Used in automated tests, in click tracking, to return directly the content
             * of a file instead of redirecting to it.
             */
            public static final String NOATTACHMENT = "noattachment";
            
            /** Feature page click type */
            public static final String TYPE_FP = "FP";
            
            /**
             * Parameter names matching the field names of the
             * {@link Result} class
             * 
             * @since 12.4
             */
            public static class Result {
                /** URL of the result in the index */
                public static final String INDEX_URL = "indexUrl";
                
                /** Title of the result */
                public static final String TITLE = "title";
                
                /** Summary of the result */
                public static final String SUMMARY = "summary";
                
                /** Live URL to access the result */
                public static final String LIVE_URL = "liveUrl";
            }
        }

        /**
         * Common request parameters used in the {@link ResultsCartController}
         */
        public static class Cart {

            /** URL of the result being added/removed */
            public static final String URL = Cache.URL;
        }

        /**
         * Common request parameter names used in the <tt>serve-*</tt>
         * controllers
         * 
         * @since 11.0
         */
        public static class Serve {
            /** URI of the document to serve */
            public static final String URI = "uri";
            
            /** Location of the file on disk */
            public static final String DOC = "doc";
            
            /** Authentication token */
            public static final String AUTH = Click.AUTH;
        }
        
        /**
         * Common request parameter names used in
         * contextual navigation.
         * 
         * @since 11.0
         *
         */
        public static class ContextualNavigation {
            /** Name of the last clicked suggestion */
            public static final String CN_CLICKED = "clicked_fluster";
            
            /**
             * Prefix for the previously clicked
             * suggestions (cluster0, cluster1, etc).
             */
            public static final String CN_PREV_PREFIX = "cluster";
            
            /**
             * Pattern to use to find previously clicked
             * suggestion parameters.
             */
            public static final Pattern CN_PREV_PATTERN = Pattern.compile(CN_PREV_PREFIX + "\\d+");
        }
    }
    
}
