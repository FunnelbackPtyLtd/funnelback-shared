package com.funnelback.publicui.search.model.collection;

import groovy.lang.Script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.funnelback.common.config.Collection.Type;
import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.paramtransform.TransformRule;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * <p>A search collection.</p>
 * 
 * <p>A collection contains several configuration information.</p>
 * 
 * @since 11.0
 */
@NoArgsConstructor
@ToString
@JsonIgnoreProperties({"parametersTransforms", "configuration", "quickLinksConfiguration", "hookScriptsClasses", "cartProcessClass"})
@Log4j
public class Collection {

    public static final String COLLECTION_ID_PATTERN = "[\\w-]+"; 
    
    /**
     * <p>Groovy Hook scripts names.</p>
     * 
     * <p>Each collection can have a set of Groovy scripts that
     * are run at various stages of the search lifecycle.</p>
     */
    public static enum Hook {
        pre_datafetch, post_datafetch, pre_process, post_process, extra_searches,
        pre_cache;
        
        /**
         * <p>Name of the Groovy variable that will contain the search transaction,
         * from whithin a Groovy script.</p>
         */
        public static final String SEARCH_TRANSACTION_KEY = "transaction";
        
        /**
         * Name of the Groovy variable that will contain the collection,
         * from within a cache Groovy script.
         */
        public static final String COLLECTION_KEY = "collection";
        
        /**
         * Name of the Groovy variable that will contain the document (key + content),
         * from within a cache Groovy script.
         * 
         * @since v12.4
         */
        public static final String DOCUMENT_KEY = "document";
        
        /**
         * Name of the Groovy variable that will contain the document metadata
         * from within a cache Groovy script.
         * 
         * @since v12.4
         */
        public static final String METADATA_KEY = "metaData";
    }
    
    /**
     * <p>Collection id (technical name).</p>
     * 
     * <p>Identical to the name of the collection folder
     * under <code>$SEARCH_HOME/conf/</code> or <code>$SEARCH_HOME/data/</code></p>
     */
    @javax.validation.constraints.Pattern(regexp=COLLECTION_ID_PATTERN)
    @Getter private String id;
    
    /**
     * <p>Collection configuration data.</p>
     * 
     * <p>Contains <code>collection.cfg</code> values. Can be accessed
     * using <code>configuration.value(KEY)</code> such as
     * <code>configuration.value("query_processor_options")</code>.</p>
     **/
    @XStreamOmitField
    @Getter private Config configuration;
    
    /** Quick Links configuration (<code>quicklinks.cfg</code>) */
    @XStreamOmitField
    @Getter @Setter private Map<String, String> quickLinksConfiguration;
    
    /**
     * <p>Search profiles. The key is the profile ID.</p>
     * 
     * <p>Each collection can have multiple search profiles. 2 profiles are
     * provided with each new collections: <code>_default</code> and
     * <code>_default_preview</code>, used in the preview / publish system.</p>
     */
    @Getter private final Map<String, Profile> profiles = new HashMap<String, Profile>();
    
    /** Faceted navigation configuration in <code>conf/[collection]/faceted_navigation.cfg</code> */
    @Getter @Setter private FacetedNavigationConfig facetedNavigationConfConfig;
    
    /** Faceted navigation configuration in <code>data/[collection]/live/idx/faceted_navigation.cfg</code> */
    @Getter @Setter private FacetedNavigationConfig facetedNavigationLiveConfig;
    
    /**
     * List of terms that the TextMiner system should not extract entities for.
     * 
     * @since 12.0
     */
    @Getter private final Set<String> textMinerBlacklist = new HashSet<String>();
    
    /**
     * <p>On meta collections, list of sub collections IDs.<p>
     * 
     * <p>Read from <code>conf/[collection]/meta.cfg</code>.</p>
     */
    @Getter @Setter private String[] metaComponents = new String[0];
    
    /**
     * <p><em>Internal use</em>: List of query string parameters transformations, previously known
     * as CGI Transforms in the Classic UI.</p>
     * 
     * <p>Read from <code>conf/[collection]/cgi_transform.cfg</code>.</p>
     */
    @XStreamOmitField
    @Getter @Setter private List<TransformRule> parametersTransforms = new ArrayList<TransformRule>();
    
    /**
     * <p><em>Internal use</em>: List of Groovy hook scripts for this collection.</p>
     * 
     * <p>The key is the name of the script ({@link Hook}, the value is the
     * implementation class (compiled Groovy script).</p>
     */
    @XStreamOmitField
    @Getter private final Map<Hook, Class<Script>> hookScriptsClasses = new HashMap<Hook, Class<Script>>();
    
    /**
     * <p><em>Internal use</em>: Groovy class to use to process the results cart</p>
     * 
     * @since 13.0
     */
    @XStreamOmitField
    @Getter @Setter private Class<?> cartProcessClass;
    
    /**
     * Get the collection type.
     * @return The collection type, or unknown.
     */
    public Type getType() {
        Type out = Type.unknown;
        if (configuration != null && configuration.hasValue(Keys.COLLECTION_TYPE)) {
            try {
                out = Type.valueOf(configuration.value(Keys.COLLECTION_TYPE));
            } catch (IllegalArgumentException iae) {
                log.warn("Unkown collection type: '" + configuration.value(Keys.COLLECTION_TYPE) + "'", iae);
            }
        }
        return out;
    }
    
    public Collection(String id, Config config) {
        this.id = id;
        this.configuration = config;
    }
    
}
