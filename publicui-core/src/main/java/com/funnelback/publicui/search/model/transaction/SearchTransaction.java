package com.funnelback.publicui.search.model.transaction;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.function.Function;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.funnelback.publicui.search.model.transaction.session.SearchSession;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * <p>Represents a full search transaction consisting of 
 * a question, a response and possible error.</p>
 * 
 * <p>This is the root of the data model.</p>
 * 
 * @since 11.0
 */
@NoArgsConstructor
@JsonIgnoreProperties({"extraSearchesTasks", "extraSearchesQuestions"})
public class SearchTransaction {

    /**
     * <em>Internal use</em>: Keys for internal extra searches.
     */
    public static enum ExtraSearches {
        /**
         * Faceted navigation extra search when the full facets
         * list mode is enabled.
         */
        FACETED_NAVIGATION,
        /**
         * Extra search for the content optimiser when it selects
         * a single document
         */
        CONTENT_OPTIMISER_SELECT_DOCUMENT;
    }
    
    /** The question containing the input parameters. */
    @Getter private SearchQuestion question;
    
    /** The response containing result data. */
    @Getter private SearchResponse response;
    
    /** Any error if the search wasn't successful. */
    @Getter @Setter private SearchError error;
    
    /**
     * User session data, might be null if not enabled.
     * 
     * @since v12.5
     */
    @Getter @Setter private SearchSession session;

    /**
     * Build a new transaction
     * @param sq {@link SearchQuestion}
     * @param sr {@link SearchResponse}
     */
    public SearchTransaction(SearchQuestion sq, SearchResponse sr) {
        this.question = sq;
        this.response = sr;
    }
    
    /**
     * <p>Any additional extra search transactions performed during this transaction.<p>
     * 
     * <p>To access result data from an extra search source, use the name
     * of this source as a key on this Map.</p>
     * 
     * @see <code>ui.modern.extra_searches</code>
     */
    @Getter private final Map<String, SearchTransaction> extraSearches = new HashMap<String, SearchTransaction>();

    /**
     * <p><em>Internal use</em>: Additional {@link SearchQuestion}s to process as extra searches.</p>
     * 
     * <p>These questions will be submitted in parallel to the main search.</p>
     */
    @XStreamOmitField
    @Getter private final Map<String, SearchQuestion> extraSearchesQuestions = new HashMap<String, SearchQuestion>();
    
    /**
     * <em>Internal use</em>: Holds the extra searches tasks being executed.
     */
    @XStreamOmitField
    @Getter private final Map<String, FutureTask<SearchTransaction>> extraSearchesTasks = new HashMap<String, FutureTask<SearchTransaction>>();
    
    /**
     * Custom data placeholder allowing any arbitrary data to be
     * stored by hook scripts.
     */
    @Getter private final Map<String, Object> customData = new HashMap<String, Object>();
    
    /**
     * Adds an {@link SearchQuestion} to be processed as an extra search.
     * @param key Name of the extra source (see <code>ui.modern.extra_searches</code>).
     * @param q The {@link SearchQuestion}
     */
    public void addExtraSearch(String key, SearchQuestion q) {
        extraSearchesQuestions.put(key, q);
    }    
    
    /**
     * @return true if the {@link #question} is not null.
     */
    public boolean hasResponse() { return response != null; }
    
    /**
     * @return true if the {@link #response} is not null.
     */
    public boolean hasQuestion() { return question != null; }
    
}
