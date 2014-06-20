package com.funnelback.publicui.search.model.curator.trigger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * <p>
 * A trigger which activates when the given substring is contained within the
 * current query.
 * </p>
 * 
 * <p>
 * Case is considered important in the matching (so a trigger with substring
 * "Bob" will not match the query "bob and jane", but "bob" would).
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class QuerySubstringTrigger implements Trigger {

    /**
     * The substring to check for in the current query.
     */
    @Getter
    @Setter
    private String triggerSubstring;

    /**
     * Return true if the current query in searchTransaction contains the
     * triggerSubstring, otherwise return false.
     */
    @Override
    public boolean activatesOn(SearchTransaction searchTransaction) {
        String query = searchTransaction.getQuestion().getQuery();
        return query.contains(triggerSubstring);
    }

}