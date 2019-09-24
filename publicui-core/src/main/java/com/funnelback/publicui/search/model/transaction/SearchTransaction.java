package com.funnelback.publicui.search.model.transaction;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.search.model.transaction.session.SearchSession;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * <p>Represents a full search transaction consisting of 
 * a question, a response and possible error.</p>
 * 
 * <p>This is the root of the data model.</p>
 * 
 * @since 11.0
 */
@Log4j2
@NoArgsConstructor
@JsonIgnoreProperties({"extraSearchesTasks", "extraSearchesQuestions", "extraSearchesAproxTimeSpent"})
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
    
    /**
     * Holds the extra search name
     * 
     * <p>For logging only</p>
     * 
     */
    @XStreamOmitField @JsonIgnore
    @Getter @NonNull private Optional<String> extraSearchName = Optional.empty();
    
    /**
     * Holds the parent search transaction from which this search transaction was created under.
     * 
     * @since 15.14.0.37
     */
    @XStreamOmitField @JsonIgnore
    @Getter @NonNull private Optional<SearchTransaction> parentTransaction = Optional.empty();
    
    public void setExtraSearchNameAndParentTransaction(Optional<String> extraSearchName, Optional<SearchTransaction> parentTransaction) {
        this.extraSearchName = extraSearchName;
        this.parentTransaction = parentTransaction;
    }
    
    /**
     * Gets the name of the extra search this search should be considered to be under.
     * 
     * The result of this should be used when modifying a particular extra search. As
     * Funnelback may create extra searches under an existing search, for example 
     * for faceted navigation, this could be used to work out if the search transaction
     * should be modified.
     * 
     * @since 15.14.0.37
     * 
     * @return The name of the extra search that is running or the name of the EXTRA_SEARCH
     * from which this search was created from. If this search is not or does not belong to
     *  an extra search empty will be returned. 
     */
    @JsonIgnore
    public Optional<String> getEffectiveExtraSearchName() {
        Optional<SearchTransaction> st = Optional.of(this);
        while(st.isPresent()) {
            // If the question is null, should this really return an optional?
            if(st.get().getQuestion() == null) return Optional.empty();
            if(st.get().getQuestion().getQuestionType() == SearchQuestionType.EXTRA_SEARCH) {
                return st.get().getExtraSearchName();
            }
            st = st.get().getParentTransaction();
        }
        return Optional.empty();
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
     * How much time (ms) has been spent in executing extra searches.
     * 
     */
    @XStreamOmitField @Getter 
    private final AtomicLong extraSearchesAproxTimeSpent = new AtomicLong(0);
    
    /**
     * Set true when at least one extra search was unable to complete.
     * <p>This can happen if extra searches take too long to run or if an error occurred within
     * the extra search.</p>
     */
    @Getter @Setter
    private boolean anyExtraSearchesIncomplete = false;

    /**
     * <p><em>Internal use</em>: Additional {@link SearchQuestion}s to process as extra searches.</p>
     * 
     * <p>These questions will be submitted in parallel to the main search.</p>
     */
    @XStreamOmitField @Getter
    private final Map<String, SearchQuestion> extraSearchesQuestions = new HashMap<String, SearchQuestion>();
    
    
    /**
     * <em>Internal use</em>: Holds the extra searches tasks being executed.
     */
    @XStreamOmitField
    @Getter private final Map<String, FutureTask<SearchTransaction>> extraSearchesTasks = new ConcurrentHashMap<String, FutureTask<SearchTransaction>>();
    
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
