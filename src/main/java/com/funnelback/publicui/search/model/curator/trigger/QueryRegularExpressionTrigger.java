package com.funnelback.publicui.search.model.curator.trigger;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.funnelback.publicui.search.model.curator.HasNoBeans;
import com.funnelback.publicui.search.model.curator.config.Configurer;
import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

/**
 * A trigger which activates when the current query matches the given regular
 * expression.
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Log4j2
public final class QueryRegularExpressionTrigger implements Trigger, HasNoBeans {

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
        if(triggerPattern == null) return false;
        String query = Trigger.queryToMatchAgainst(searchTransaction);
        try {
            return Pattern.compile(triggerPattern).matcher(query).find();
        } catch (PatternSyntaxException e) {
            log.error("Invalid syntax in curator query regular expression trigger", e);
            return false;
        }
    }

    /** Configure this trigger (expected to autowire in any dependencies) */
    @Override
    public void configure(Configurer configurer) {
        configurer.configure(this);
    }
}
