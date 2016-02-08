package com.funnelback.publicui.search.model.curator.config;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.Keys;
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
     * Reads Keys.ModernUI.Curator.QUERY_PARAMETER_PATTERN, finds matching input parameters and returns a single query for matching against.
     * 
     * Values from each param are separated by a single space, and the values are ordered based on the Java String sort order of the keys.
     */
    public static String queryToMatchAgainst(SearchTransaction searchTransaction) {
        // Adrian and I discussed a bit whether each value should me matched individually or
        // if we should join them (in some order) and match across everything. In the end, combining
        // them seemed like it would be useful sometimes, and would likely still work for anyone
        // assuming the values were matched individually. Using the key sort order because
        // nothing else seems like it would be easy to explain to users. -- Matt
        String queryParameterPatternString = searchTransaction.getQuestion().getCollection().getConfiguration()
            .value(Keys.ModernUI.Curator.QUERY_PARAMETER_PATTERN, DefaultValues.ModernUI.Curator.QUERY_PARAMETER_PATTERN);
        
        Pattern p;
        try {
            p = Pattern.compile(queryParameterPatternString);
        } catch (PatternSyntaxException e) {
            Logger log = org.apache.logging.log4j.LogManager.getLogger(Trigger.class);
            log.error(Keys.ModernUI.Curator.QUERY_PARAMETER_PATTERN + " is not a valid regular expression - Curator will not trigger on any query parameters", e);
            return "";
        }
        
        return searchTransaction.getQuestion().getInputParameterMap().keySet().stream()
            .filter(k -> p.matcher(k).matches())
            .sorted()
            .map(k -> searchTransaction.getQuestion().getInputParameterMap().get(k))
            .collect(Collectors.joining(" "));
    }
}
