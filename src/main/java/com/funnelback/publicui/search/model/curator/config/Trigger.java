package com.funnelback.publicui.search.model.curator.config;

import static com.funnelback.config.keys.Keys.FrontEndKeys;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.funnelback.config.configtypes.service.ServiceConfigOptionDefinition;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * <p>
 * Triggers control when curator actions are performed (or not).
 * </p>
 * 
 * <p>
 * Implementing classes must define, based on the current search transaction,
 * whether the trigger activates or not.
 * </p>
 * 
 * <p>
 * NOTE: To avoid displaying the full package name for the implementing class in
 * the curator config file add the implementing class to the aliasedTriggers
 * array in publicui-web's
 * com.funnelback.publicui.search.service.resource.impl.CuratorConfigResource
 * </p>
 * 
 * @since 13.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeIdResolver(TriggerTypeIdResolver.class)
public interface Trigger {

    /**
     * @param searchTransaction Current search transaction
     * @return true if this trigger should activate on the given
     * searchTransaction, and false otherwise.
     */
    public boolean activatesOn(SearchTransaction searchTransaction);
    
    /**
     * Perform any configuration required (e.g. autowiring beans needed by the Trigger).
     */
    public void configure(Configurer configurer);

    /**
     * Reads FrontEnd.ModernUi.Curator.QUERY_PARAMETER_PATTERN, finds matching input parameters and returns a single query for matching against.
     * 
     * Values from each param are separated by a single space, and the values are ordered based on the Java String sort order of the keys.
     */
    public static String queryToMatchAgainst(SearchTransaction searchTransaction) {
        // Adrian and I discussed a bit whether each value should be matched individually or
        // if we should join them (in some order) and match across everything. In the end, combining
        // them seemed like it would be useful sometimes, and would likely still work for anyone
        // assuming the values were matched individually. Using the key sort order because
        // nothing else seems like it would be easy to explain to users. -- Matt
        ServiceConfigOptionDefinition<String> queryParameterPatternKey = FrontEndKeys.ModernUi.CURATOR.QUERY_PARAMETER_PATTERN;
        String queryParameterPatternString = searchTransaction.getQuestion().getCurrentProfileConfig().get(queryParameterPatternKey);

        Pattern p;
        try {
            p = Pattern.compile(queryParameterPatternString);
        } catch (PatternSyntaxException e) {
            Logger log = org.apache.logging.log4j.LogManager.getLogger(Trigger.class);
            log.error(queryParameterPatternKey.getKey() + " is not a valid regular expression - Curator will not trigger on any query parameters", e);
            return "";
        }
        
        Set<String> keys = new HashSet<>();
        keys.addAll(searchTransaction.getQuestion().getInputParameterMap().keySet());
        keys.add("query"); // Ensure query is always included in case it was added by a hook script
        
        return keys.stream()
            .filter(k -> p.matcher(k).matches())
            .sorted()
            .map(k -> {
                if (k.equals("query")) {
                    if (searchTransaction.getQuestion().getQuery() != null) {
                        return searchTransaction.getQuestion().getQuery(); 
                    } else {
                        return "";
                    }
                } else {
                    return searchTransaction.getQuestion().getInputParameterMap().get(k);
                }
            })
            .collect(Collectors.joining(" "));
    }
}
