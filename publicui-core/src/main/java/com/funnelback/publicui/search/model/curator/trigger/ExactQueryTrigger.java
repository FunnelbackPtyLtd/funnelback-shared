package com.funnelback.publicui.search.model.curator.trigger;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.publicui.search.model.curator.config.Configurer;
import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * <p>
 * A trigger which activates only when the current query exactly matches the
 * given trigger query.
 * </p>
 * 
 * <p>
 * Case between the queries may optionally be ignored.
 * </p>
 * 
 * <p>
 * The word order within the query is always considered significant.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class ExactQueryTrigger implements Trigger {

    /** The string the given query must match for this trigger to activate. */
    @Getter
    @Setter
    private String triggerQuery;

    /**
     * Whether to consider case difference between the query and trigger as
     * important (defaults to false).
     */
    @Getter
    @Setter
    private boolean ignoreCase;

    /**
     * Return true if the query within searchTransaction exactly matches the
     * triggerQuery (subject to ignoreCase), otherwise return false.
     */
    @Override
    public boolean activatesOn(SearchTransaction searchTransaction) {
        String query = Trigger.queryToMatchAgainst(searchTransaction);
        query = query.replaceAll("\\s+", " ").trim(); // Remove any extra whitespace
        if (ignoreCase) {
            return query.equalsIgnoreCase(triggerQuery);
        } else {
            return query.equals(triggerQuery);
        }
    }

    /** Configure this trigger (expected to autowire in any dependencies) */
    @Override
    public void configure(Configurer configurer) {
        configurer.configure(this);
    }
}
