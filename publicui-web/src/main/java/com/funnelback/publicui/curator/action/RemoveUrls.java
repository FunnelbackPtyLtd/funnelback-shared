package com.funnelback.publicui.curator.action;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.publicui.search.model.curator.config.Action;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.google.common.base.Joiner;

/**
 * <p>
 * Action to remove a set of URLs within the search results, causing them to be
 * excluded from the result set even if they match the given query. This would
 * generally be done to eliminate a duplicate result which is being displayed
 * through a UrlAdvert or similar. If a URL should be permanently removed it is
 * preferable to either kill it in the index or to avoid gathering it in the
 * first place.
 * </p>
 * 
 * <p>
 * If this action is performed multiple times the list of URLs to be removed
 * will be combined.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RemoveUrls implements Action {

    /** The list of URLs to be removed from the results */
    @Getter
    @Setter
    private List<String> urlsToRemove;

    /**
     * Appends this instance's urlsToRemove to the list of URLs for removal
     * which will be given to padre.
     */
    @Override
    public void performAction(SearchTransaction searchTransaction, Phase phase) {
        String newRemovedUrls = Joiner.on(" ").join(urlsToRemove);
        addUrlsStringToRemoveList(searchTransaction, newRemovedUrls);
    }

    /**
     * Add URLs for removal to the searchTranaction. This must occur during the
     * input lifecycle (after the padre query is run it will be too late).
     * 
     * @param searchTransaction
     *            The SearchTransaction object to be modified
     * @param urlsToRemove
     *            A space separated list of URLs to be removed
     */
    static void addUrlsStringToRemoveList(SearchTransaction searchTransaction, String urlsToRemove) {
        String urlsAlreadyRemoved = "";
        if (searchTransaction.getQuestion().getAdditionalParameters().containsKey(RequestParameters.REMOVE_URLS)) {
            urlsAlreadyRemoved = searchTransaction.getQuestion().getAdditionalParameters()
                .get(RequestParameters.REMOVE_URLS)[0]
                + " ";
        }

        searchTransaction.getQuestion().getAdditionalParameters()
            .put(RequestParameters.REMOVE_URLS, new String[] { urlsAlreadyRemoved + urlsToRemove });
    }

    /**
     * URL removal occurs during the input phase (as the actual removal is
     * performed by augmenting the request to padre.
     */
    @Override
    public boolean runsInPhase(Phase phase) {
        return phase.equals(Phase.INPUT);
    }
}
