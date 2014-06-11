package com.funnelback.publicui.curator.trigger;

import java.util.regex.Pattern;

import org.springframework.context.ApplicationContext;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * A trigger which activates when the current query matches the given regular
 * expression.
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
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
    public boolean activatesOn(SearchTransaction searchTransaction, ApplicationContext context) {
        String query = searchTransaction.getQuestion().getQuery();
        return Pattern.compile(triggerPattern).matcher(query).find();
    }

}
