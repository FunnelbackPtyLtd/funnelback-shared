package com.funnelback.publicui.search.model.transaction.session;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <p>Contains data associated with the current user
 * and search session.</p>
 * 
 * <p>Session features must be enabled on the collection
 * and the application container session management must be
 * properly configured.</p>
 * 
 * @since 12.5
 */
@RequiredArgsConstructor
public class SearchSession {

    /**
     * <p>User performing the search.</p>
     * 
     * <p>Will be the same across multiple searches for a given user.</p>
     */
    @Getter final private SearchUser searchUser;

    /**
     * Search history of the user for the current collection.
     */
    @Getter final private List<SearchHistory> searchHistory = new ArrayList<>();
    
    /**
     * Click history of the user for the current collection.
     */
    @Getter final private List<ClickHistory> clickHistory = new ArrayList<>();

    /**
     * Results cart of the user for the current collection
     * 
     * @since 12.4
     */
    @Getter final private List<CartResult> resultsCart = new ArrayList<>();

}
