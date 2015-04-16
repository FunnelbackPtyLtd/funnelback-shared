package com.funnelback.publicui.search.model.curator.trigger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.publicui.search.model.curator.config.Configurer;
import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * <p>
 * A trigger which activates when a CGI parameter has a particular value.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CgiParameterTrigger implements Trigger {
    
    /**
     * The CGI parameter to check
     */
    @Getter
    @Setter
    private String parameter;

    /**
     * The 'value' to check for in the CGI parameter
     */
    @Getter
    @Setter
    private String value;

    /**
     * The type of matching to be performed between each CGI parameter value
     * (haystack) and the given value parameter (needle).
     */
    @Getter
    @Setter
    private StringMatchType matchType = StringMatchType.EXACT;
    
    /**
     * Check whether the given searchTransaction contains a parameter of the given name
     * which matches (as defined by matchType) the value.
     * 
     * In the case of multiple parameters, each is checked, and false returned only if none match.
     */
    @Override
    public boolean activatesOn(SearchTransaction searchTransaction) {
        if (searchTransaction.getQuestion().getRawInputParameters().containsKey(parameter)) {
            for (String haystack : searchTransaction.getQuestion().getRawInputParameters().get(parameter)) {
                if (matchType.matches(value,  haystack)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Configure this trigger (expected to autowire in any dependencies) */
    @Override
    public void configure(Configurer configurer) {
        configurer.configure(this);
    }
}
