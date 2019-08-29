package com.funnelback.publicui.search.model.curator.action;

import java.util.ArrayList;
import java.util.List;

import com.funnelback.publicui.search.model.curator.HasNoBeans;
import com.funnelback.publicui.search.model.curator.config.Action;
import com.funnelback.publicui.search.model.curator.config.Configurer;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>Action to transform the user's query based on a regular expression replacement.</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public final class TransformQuery implements Action, HasNoBeans {

    /** The regular expression to match */
    @Getter @Setter private String match;

    /** The regular expression replacement */
    @Getter @Setter private String replacement;

    /**
     * Whether to apply the transformation to all other query parameters (such
     * as meta_x and query_or), in addition to the simple query one.
     */
    @Getter @Setter private Boolean applyToAllQueryParameters = false;

    /**
     * Replace occurrences of target with replacement within the question's query and metaParameters.
     */
    @Override
    public void performAction(SearchTransaction searchTransaction, Phase phase) {
        searchTransaction.getQuestion().setQuery(transform(match, replacement, searchTransaction.getQuestion().getQuery()));
        
        if (applyToAllQueryParameters) {
            List<String> newMetaParameters = new ArrayList<>();
            for (String metaQuery : searchTransaction.getQuestion().getMetaParameters()) {
                newMetaParameters.add(transform(match, replacement, metaQuery));
            }
            searchTransaction.getQuestion().getMetaParameters().clear();
            searchTransaction.getQuestion().getMetaParameters().addAll(newMetaParameters);
        }
    }

    /**
     * Returns a new string containing content with all occurrences of target replaced with replacement.
     */
    private static String transform(String match, String replacement, String content) {
        return content.replaceAll(match, replacement);
    }

    /**
     * Query term addition occurs in the INPUT phase since it must modify
     * the query before it is run by padre.
     */
    @Override
    public boolean runsInPhase(Phase phase) {
        return phase.equals(Phase.INPUT);
    }

    /** Configure this action (expected to autowire in any dependencies) */
    @Override
    public void configure(Configurer configurer) {
        configurer.configure(this);
    }
}
