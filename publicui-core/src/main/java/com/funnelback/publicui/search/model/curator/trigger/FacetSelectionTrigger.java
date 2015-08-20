package com.funnelback.publicui.search.model.curator.trigger;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.publicui.search.model.curator.config.Configurer;
import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.publicui.search.model.facetednavigation.FacetParameter;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.utils.FacetedNavigationUtils;

/**
 * <p>
 * A trigger which activates when a particular facet category is selected.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class FacetSelectionTrigger implements Trigger {
    
    /**
     * The name of the facet to check.
     */
    @Getter
    @Setter
    private String facetName;

    /**
     * The category value to be checked within the facet.
     */
    @Getter
    @Setter
    private String categoryValue;

    /**
     * The type of matching to be performed between each selected category in the facetName facet
     * (haystack) and the given categoryValue (needle).
     */
    @Getter
    @Setter
    private StringMatchType matchType = StringMatchType.EXACT;

    /**
     * Check whether the given searchTransaction contains a selected category in
     * the facetName facet which matches (as defined by matchType) the
     * categoryValue parameter.
     */
    @Override
    public boolean activatesOn(SearchTransaction searchTransaction) {
        List<FacetParameter> facetParamaters = FacetedNavigationUtils.getFacetParameters(searchTransaction.getQuestion());
        
        for (FacetParameter facetParameter: facetParamaters) {
            if (facetParameter.getName().equals(facetName)) {
                for (String haystack : facetParameter.getValues()) {
                    if (matchType.matches(categoryValue, haystack)) {
                        return true;
                    }
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
