package com.funnelback.publicui.search.model.transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import java.security.Principal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.funnelback.publicui.search.model.collection.SearchPackageConfig;
import com.funnelback.publicui.search.model.collection.ServiceConfig;
import com.funnelback.publicui.search.model.geolocation.Location;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.MultimapBuilder.ListMultimapBuilder;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

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
@JsonIgnoreProperties({"principal","maxPadrePacketSize", "currentProfile", "currentProfileConfig"})
public class SearchQuestion {

    /**
     * Execution context of the Search UI. Can be "admin"
     * or "public", depending if the search is ran on the admin
     * port or public port.
     * 
     * @since 15.12
     */
    @Getter @Setter private ExecutionContext executionContext;

    /**
     * Headers provided with the incoming search request
     * 
     * @since 15.16
     */
    @JsonIgnore @XStreamOmitField
    @Getter @Setter private ListMultimap<String, String> requestHeaders = ListMultimapBuilder.hashKeys().arrayListValues().build();

    /**
     * <p>Query terms.</p>
     * 
     * <p>This may be transformed before being passed to
     * PADRE, depending on the configuration.</p>
     */
    @Getter @Setter private String query;
    
    /**
     * Original query, as entered by the user.
     */
    @Getter @Setter private String originalQuery;
    
    /**
     * Searched {@link Collection}.
     */
    @Getter @Setter private SearchPackageConfig collection;

    /**
     * The profile to search with, defaulting to "_default".
     */
    @NonNull
    @Getter @Setter private String profile = "_default";

    /**
     * The profile which will be used for the request. This one will always correspond to the
     * name of a profile which exists within the selected collection.
     * 
     * Note that this may differ from profile if the requested profile does not actually exist
     * on disk (in which case it will be set to DefaultValues.DEFAULT_PROFILE, as that is what's used).
     * 
     * @since 15.12
     */
    // We could instead just overwrite profile with this 'real on disk' value, but there's some
    // concern that doing so would create backwards compatibility issues.
    @XStreamOmitField
    @Getter @Setter private String currentProfile;

    /**
     * Returns the (modern) config of the currentProfile
     */
    // XStream won't serialize getters it seems, excluded from Jackson on the class
    public ServiceConfig getCurrentProfileConfig() {
        return serviceConfigProvider.apply(this);
    }
    
    @Setter @JsonIgnore @XStreamOmitField
    private Function<SearchQuestion, ServiceConfig> serviceConfigProvider = sq -> null;
    
    /**
     * Specific component of a meta-collection to query
     */
    @Getter @Setter private String[] clive;
    
    /**
     * Display form (template)
     */
    @NonNull    
    @Getter @Setter private String form = "simple";
    
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
     * {@link #inputParameters}. Those parameters will be processed by the Modern UI 
     * (For example to inject faceted navigation constraints).</p> 
     * 
     * <p>Note that this map is populated <strong>before</strong> the first hook script is run: Any
     * additional parameter injected in {@link #inputParameters}
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
     * Query processor options that are guaranteed to apply.
     * 
     * <p>Internal use only</p>
     * @since 15.12
     */
    @JsonIgnore @XStreamOmitField
    @Getter final private PriorityQueryProcessorOptions priorityQueryProcessorOptions = new PriorityQueryProcessorOptions();

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
     * For example: "Location" =&gt; ("Sydney", "Melbourne").</p>
     */
    @Getter final private Map<String, List<String>> selectedCategoryValues = new HashMap<String, List<String>>();
    
    /**
     * Query constraints to apply for faceted navigation
     * (In addition to other query expressions).
     */
    @Getter @Setter private List<String> facetsQueryConstraints = new ArrayList<String>();
    
    /**
     * The collections to restrict search to for faceted navigation.
     * (In addition to other clive constraints).
     * @since 15.12
     */
    @Getter @Setter @NonNull private Optional<List<String>> facetCollectionConstraints = Optional.empty();
    
    /**
     * GScope constraints to apply for faceted navigation
     * (In addition to existing gscope constraints).
     */
    @Setter @Getter private String facetsGScopeConstraints;
    
    /**
     * Whether the request is impersonated (Document Level Security)
     */
    @Getter @Setter private boolean impersonated;
    
    /**
     * Request identifier to log for this transaction. Depending on the collection configuration
     * it can be an IP address, an md5 hash of the address, nothing ("-") or null.
     * 
     *  @since 12.4 - Renamed from <code>userId</code>
     *  @since 12.5 - Renamed from <code>userIdToLog</code>
     */
    @Getter @Setter private String requestId = "-";
    
    /**
     * Defines if the query should be logged or not, or if it should fall back to default.
     * 
     * If false this will prevent the query from being recorded in
     * query logs as well as session history. 
     * 
     * If not set this will result in the defaults being used. For padre the default 
     * is to follow what is in collection.cfg, profile.cfg or padre_opts.cfg, otherwise
     * if the log option is not set in any of those files padre will log the query.
     * 
     * This will typically be set false for system generated searches or
     * for extra searches. This may be set from groovy scripts.
     * 
     * This can be set to false with cgi parameters on the admin side
     * if the request is authenticated. To do this set &log=false in
     * the URL.
     * 
     * @since 15.14
     */
    @Getter @Setter private Optional<Boolean> logQuery = Optional.empty();
    
    /**
     * <p>Raw input parameters</p>
     * 
     * <p>Contains all the input parameters (query string / request parameters).</p>
     * 
     * <p>Be aware that the value type is <code>ListMultimap</code>, thus a single 
     * key can have multiple values also {@link ListMultimap#get(Object)} will never 
     * return null.</p>
     * 
     * <p> In freemarker to get the first value for a key or empty:
     * <pre>{@code 
     * question.inputParameters["query"]?first!
     * }</pre>
     * </p>
     * 
     * <p> In freemarker to test if the key exists and therefore has values:
     * <pre>{@code 
     * question.inputParameters?keys?seq_contains("query")
     * }</pre>
     * </p>
     * 
     * <p> In Java to replace all values for a key:
     * <pre>{@code 
     * inputParameters.replaceValues("query", List.of("my new query"))
     * }</pre>
     * 
     * </p>
     * A clone of this map can be made with {@link #getInputParametersCopy()}.
     * 
     * <p>To convert such a map into a query string suitable for URLs,
     * see {@link QueryStringUtils#toString(ListMultimap, boolean)}</p>
     * 
     * @since 16.0
     */
    @Getter
    private final ListMultimap<String, String> inputParameters = MultimapBuilder.hashKeys().arrayListValues().build();
    
    /**
     * <p>Returns a copy of the {@link #getInputParameters()} map</p>
     * 
     * <p>Be aware that the value type is <code>ListMultimap</code>, thus a single 
     * key can have multiple values also {@link ListMultimap#get(Object)} will never 
     * return null.</p>
     * 
     * @return 
     */
    @JsonIgnore
    public ListMultimap<String, String> getInputParametersCopy() {
        return MultimapBuilder.hashKeys().arrayListValues().build(inputParameters);
    }
    
    /**
     * <p>Sets the query string parameter map.</p>
     * 
     * <p>This is for internal use only and shouldn't be called outside of
     * initializing the search question. Changing the query string map will
     * affect all the URLs that are constructed in the data model</p>
     * 
     * @since 15.26
     */
    @Setter @NonNull private ListMultimap<String, String> queryStringMap = MultimapBuilder.hashKeys().arrayListValues().build();
    
     /**
     * <p>Query string parameters as a Map</p>
     * 
     * <p>Return a copy of the internal map representing the query string,
     * for easy manipulation of individual parameters rather than having to decode
     * the query string.</p>
     * 
     * <p>The map is a copy intended to be modifiable to add/remove/update
     * query string parameters.</p>
     * 
     * <p>To convert such a map into a query string suitable for URLs,
     * see {@link QueryStringUtils#toString(MultiMap, boolean)}</p>
     * 
     * @return Query string parameters
     * 
     * @since 15.10
     */
    @JsonIgnore
    public ListMultimap<String, String> getQueryStringMapCopy() {
        return MultimapBuilder.hashKeys().arrayListValues().build(queryStringMap);
    }


    /**
     * <p>Indicates the 'type' of question, which may trigger special processing in the search lifecycle.</p>
     * 
     * <p>e.g. Extra searches or Content Auditor requests.</p>
     */
    @Getter @Setter private SearchQuestionType questionType = SearchQuestionType.SEARCH;    

    /**
     * <p>Indicates if this question is part of the "main" search, or part of an "extra"
     * search.</p>
     * 
     * <p>This is needed to perform some actions only on the main search but not for every
     * extra search.</p>
     * 
     * @deprecated  As of release 14.2, replaced by {@link #questionType}
     */
    @Deprecated
    public boolean isExtraSearch() {
        return questionType.equals(SearchQuestionType.EXTRA_SEARCH);
    }

    /**
     * <p>Sets whether this question is part of the "main" search, or part of an "extra"
     * search.</p>
     * 
     * @deprecated  As of release 14.2, replaced by {@link #questionType}
     */
    @Deprecated
    public void setExtraSearch(boolean isExtraSearch) {
        questionType = isExtraSearch ? SearchQuestionType.EXTRA_SEARCH : SearchQuestionType.SEARCH;
    }

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
     * @since 13.0 (12.4 included a MaxMind location class directly here)
     */
    @Getter
    @Setter
    private Location location;

    /**
     * <p>Principal representing the remote user for the current request.</p>
     * 
     * <p>Will be NULL if the user isn't authenticated.</p>
     *
     * <p><b>Note:</b> This is not included in the JSON / XML output
     * for security reasons</p>
     *
     * @since 12.2
     */
    @XStreamOmitField
    @Getter @Setter private Principal principal;

    /**
     * <p>Name of the host where PADRE executed the query.</p>
     * @since 14.0
     */
    @Getter @Setter private String hostname;
    
    /**
     * <p>Funnelback version</p>
     * 
     * @since 15.12
     */
    @Getter @Setter private FunnelbackVersionI funnelbackVersion;
    
    
    /**
     * <p>A optional maxPadrePacketSize which may be overridden to set the max 
     * Padre packet size that will be permitted. Generally the Config option
     * ui.modern.padre_response_size_limit_bytes should be set in preference to
     * setting this.</p>
     * @since 15.12
     */
    @XStreamOmitField
    @Getter @Setter private Optional<Integer> maxPadrePacketSize = Optional.empty();
    
    /**
     * <p>A optional timeout which when set overrides the value of collection.cfg option
     * ui.modern.padre_fork_timeout_ms, which controls how long the main
     * padre forking may take.</p>
     * 
     * @since 15.12
     */
    @XStreamOmitField
    @JsonIgnore
    @Getter @Setter private Optional<Long> padreTimeout = Optional.empty();
    
    /**
     * Custom data placeholder allowing any arbitrary data to be
     * stored by hook scripts.
     * 
     * @since 15.12
     */
    @Getter private final Map<String, Object> customData = new HashMap<>();
    
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

        /** System generated query */
        public static final String S = "s";
        
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

        /** Rank of the first result to display */
        public static final String START_RANK = "start_rank";

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
        public static final Pattern FACET_PARAM_PATTERN = Pattern.compile("^" + FACET_PREFIX.replaceAll("\\.", "\\\\.") + "([^\\|]+)(\\|(.+))");
        
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

        /** Parameter for promoting individual URLs */
        public static final String PROMOTE_URLS = "promote_urls";

        /** Parameter for removing individual URLs */
        public static final String REMOVE_URLS = "remove_urls";

        /** Parameter for enabling/disabling curator */
        public static final String CURATOR = "curator";

        /** Parameter for controlling the number of documents considered in document at a time mode */
        public static final String DAAT = "daat";

        /** Parameter indicating only fully matching documents should be returned */
        public static final String FULL_MATCHES_ONLY = "fmo";

        /** Maximum bytes of metadata to return per result */
        public static final String METADATA_BUFFER_LENGTH = "MBL";

        /** Type of result summaries padre should return */
        public static final String SUMMARY_MODE = "SM";

        /** Metadata field list to be used in producing result metadata summaries */
        public static final String SUMMARY_FIELDS = "SF";

        /** Controls the type/level of stemming which should be used */
        public static String STEM = "stem";

        /** Controls whether or not result collapsing is performed */
        public static String COLLAPSING = "collapsing";

        /** Controls the field used for collapsing */
        public static String COLLAPSING_SIGNATURE = "collapsing_sig";

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
            
            /** Document path */
            public static final String DOC = "doc";
            
            /** Offset where to read the file from */
            public static final String OFFSET = "off";
            
            /** Length of the file to read */
            public static final String LENGTH = "len";
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
         * Common request parameters used in the ResultsCartController
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
    
    /** Enum for identifying special types of search requests requiring special processing */
    @AllArgsConstructor
    public enum SearchQuestionType {
        SEARCH(false),
        SEARCH_GET_ALL_RESULTS(false),
        EXTRA_SEARCH(true), 
        CONTENT_AUDITOR(false), 
        CONTENT_AUDITOR_DUPLICATES(false), 
        ACCESSIBILITY_AUDITOR(false),
        ACCESSIBILITY_AUDITOR_ACKNOWLEDGEMENT_COUNTS(false),
        ACCESSIBILITY_AUDITOR_GET_ALL_RESULTS(false),
        /**
         * These extra searches are created for use in faceted navigation.
         * 
         * <p>Hooks should ensure that these searches are updated with any
         * changes that will manipulate the scope of the search. Hooks should 
         * avoid setting options which manipulate other query settings as it
         * may reduce performance.</p>
         */
        FACETED_NAVIGATION_EXTRA_SEARCH(true),
        ;
        
        /**
         * Is the search type an extra search one
         */
        @Getter private boolean extraSearch;
    }
    
}
