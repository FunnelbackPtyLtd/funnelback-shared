package com.funnelback.curator.action;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.funnelback.curator.action.data.UrlAdvert;
import com.funnelback.publicui.search.model.curator.config.Action;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Action to display the specified URL advert within the search results.
 */
@AllArgsConstructor
@NoArgsConstructor
public class DisplayUrlAdvert implements Action {

    /** The URL advert object to be displayed. */
    @Getter
    @Setter
    private UrlAdvert advert;

    /**
     * Adds the action's URL advert object to the curator model's exhibits and
     * removes any result with the same display URL from the main search
     * results.
     */
    @Override
    public void performAction(SearchTransaction searchTransaction, Phase phase) {
        if (phase.equals(Phase.INPUT)) {
            // Remove the URL if it exists in the results
            RemoveUrls.addUrlsStringToRemoveList(searchTransaction, advert.getDisplayUrl());
        } else if (phase.equals(Phase.OUTPUT)) {
            // Add the new URL advert
            searchTransaction.getResponse().getCuratorModel().getExhibits().add(advert);
        }
    }

    /**
     * Display URL Advert runs in both the input phase (to remove any duplicate
     * results) and the output phase (to add the advert data).
     */
    @Override
    public boolean runsInPhase(Phase phase) {
        return phase.equals(Phase.INPUT) || phase.equals(Phase.OUTPUT);
    }
}
