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
 * <p>Action to promote a set of URLs within the search results, causing them to be
 * displayed at the top of the result set in the order given.</p>
 * 
 * <p>If this action is performed multiple times the list of URLs to be promoted
 * will be appended to, meaning that the earliest invocation will 'win' in terms
 * of the final result ordering.</p>
 * 
 * <p>Note - Result promoting has no effect if sorting modes other than relevance
 * are used (e.g. date sorting is unaffected).</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PromoteUrls implements Action {

    /**
     * The list of URLs to promote (the first given URL will be returned at rank
     * one, the second and rank two and so on).
     */
    @Getter
    @Setter
    private List<String> urlsToPromote;

    /**
     * Appends this instance's urlsToPromote to the list of URLs for promotion
     * which will be given to padre.
     */
    @Override
    public void performAction(SearchTransaction searchTransaction, Phase phase) {
        String newPromotedUrls = Joiner.on(" ").join(urlsToPromote);

        String oldPromotedUrls = "";
        if (searchTransaction.getQuestion().getAdditionalParameters().containsKey(RequestParameters.PROMOTE_URLS)) {
            oldPromotedUrls = searchTransaction.getQuestion().getAdditionalParameters()
                .get(RequestParameters.PROMOTE_URLS)[0]
                + " ";
        }

        searchTransaction.getQuestion().getAdditionalParameters()
            .put(RequestParameters.PROMOTE_URLS, new String[] { oldPromotedUrls + newPromotedUrls });
    }

    /**
     * URL promotion occurs in the INPUT phase since the information about the
     * URLs to promote is simply passed down to padre when the request is
     * performed.
     */
    @Override
    public boolean runsInPhase(Phase phase) {
        return phase.equals(Phase.INPUT);
    }

}
