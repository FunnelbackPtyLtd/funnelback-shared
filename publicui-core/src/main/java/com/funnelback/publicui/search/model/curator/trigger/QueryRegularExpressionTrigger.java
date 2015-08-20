package com.funnelback.publicui.search.model.curator.trigger;

import java.util.regex.Pattern;

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
 * A trigger which activates when the current query matches the given regular
 * expression.
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class QueryRegularExpressionTrigger implements Trigger {

    /** The regular expression to run against the current query */
    @Getter
    @Setter
    private String triggerPattern;

    /**
     * Return true if the query within the searchTransaction matches the
     * triggerPatter, otherwise return false
     */
    @Override
    public boolean activatesOn(SearchTransaction searchTransaction) {
        String query = searchTransaction.getQuestion().getQuery();
        return Pattern.compile(triggerPattern).matcher(query).find();
    }

    /** Configure this trigger (expected to autowire in any dependencies) */
    @Override
    public void configure(Configurer configurer) {
        configurer.configure(this);
    }
}
