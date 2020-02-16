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
 * <p>
 * Action to replace a specified term within the users query before running it.
 * </p> <p>
 * This replacement will apply to the query (including special parameters like
 * query_and etc), and meta parameters but not to system parameters.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public final class ReplaceQueryTerm implements Action, HasNoBeans {

    /** The term to be replaced. */
    @Getter @Setter private String target;

    /** The term to replace target with. */
    @Getter @Setter private String replacement;

    /**
     * Replace occurrences of target with replacement within the question's query and metaParameters.
     */
    @Override
    public void performAction(SearchTransaction searchTransaction, Phase phase) {
        
        searchTransaction.getQuestion().setQuery(replace(target, replacement, searchTransaction.getQuestion().getQuery()));

        List<String> newMetaParameters = new ArrayList<>();
        for (String metaQuery : searchTransaction.getQuestion().getMetaParameters()) {
            newMetaParameters.add(replace(target, replacement, metaQuery));
        }
        searchTransaction.getQuestion().getMetaParameters().clear();
        searchTransaction.getQuestion().getMetaParameters().addAll(newMetaParameters);
    }

    /**
     * Returns a new string containing content with all occurrences of target replaced with replacement.
     */
    private static String replace(String target, String replacement, String content) {
        return content.replace(target, replacement);
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
