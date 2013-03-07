package com.funnelback.publicui.search.model.transaction;

import com.funnelback.common.padre.ResultPacket;
import com.funnelback.publicui.search.model.collection.Collection;

/**
 * Convenience utilities for {@link SearchTransaction}.
 * 
 * @since 11.0
 */
public class SearchTransactionUtils {

    /**
     * Checks that a transaction has a question.
     * @param st
     * @return true if the {@link SearchTransaction} is non null and has a
     *  {@link SearchQuestion}
     */
    public static boolean hasQuestion(SearchTransaction st) {
        return st != null && st.getQuestion() != null;
    }
    
    /**
     * Checks that a transaction has a question which has query.
     * @param st
     * @return true if the {@link SearchTransaction} is non null and has a {@link SearchQuestion} and
     * a non-null query.
     */
    public static boolean hasQuery(SearchTransaction st) {
        return(st != null && st.getQuestion() != null && st.getQuestion().getQuery() != null);
    }
    
    /**
     * Checks that a transaction has a question which has a collection.
     * @param st
     * @return true if the {@link SearchTransaction} is non null and has a {@link SearchQuestion} and
     * a non-null {@link Collection}.
     */
    public static boolean hasCollection(SearchTransaction st) {
        return(st != null && st.getQuestion() != null && st.getQuestion().getCollection() != null); 
    }
    
    /**
     * Checks that a transaction has question which has a query and a collection.
     * @param st
     * @return true if the {@link SearchTransaction} is non null and has a {@link SearchQuestion}, 
     * a non-null query and a non-null {@link Collection}.
     */
    public static boolean hasQueryAndCollection(SearchTransaction st) {
        return hasQuery(st) && hasCollection(st);
    }
    
    /**
     * Checks that a transaction has a response.
     * @param st
     * @return true if the {@link SearchTransaction} is non null and has a {@link SearchResponse}.
     */
    public static boolean hasResponse(SearchTransaction st) {
        return st != null && st.hasResponse();
    }
    
    /**
     * Checks that a transaction has a response which has a result packet
     * @param st
     * @return true if the {@link SearchTransaction} has a result packet, false otherwise
     */
    public static boolean hasResultPacket(SearchTransaction st) {
        return hasResponse(st) && st.getResponse().getResultPacket() != null;
    }
    
    /**
     * Checks that a transaction has a response which has results.
     * @param st
     * @return true if the {@link SearchTransaction} is non null and has a {@link SearchResponse} which
     * contains a {@link ResultPacket} and has more than zero results.
     */
    public static boolean hasResults(SearchTransaction st) {
        return hasResponse(st) && st.getResponse().hasResultPacket() && st.getResponse().getResultPacket().hasResults();
    }
    
}
