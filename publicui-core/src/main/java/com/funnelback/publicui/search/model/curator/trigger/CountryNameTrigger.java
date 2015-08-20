package com.funnelback.publicui.search.model.curator.trigger;

import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.funnelback.publicui.search.model.curator.config.Configurer;
import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.publicui.search.model.geolocation.Location;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * <p>
 * A trigger which activates only when the current request originates from a
 * country whose name exists in the targetCountries list.
 * </p>
 * 
 * <p>
 * The detection of the originating country requires geolocation to be active
 * (otherwise this trigger will never activate).
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class CountryNameTrigger implements Trigger {

    /**
     * The list of country names (case sensitive) for which this trigger should
     * activate.
     */
    @Getter
    @Setter
    private Set<String> targetCountries = new HashSet<String>();

    /**
     * Check whether the given searchTransaction originates from a country
     * listed in targetCountries. If it does, return true, otherwise false.
     */
    @Override
    public boolean activatesOn(SearchTransaction searchTransaction) {
        Location location = searchTransaction.getQuestion().getLocation();
        return targetCountries.contains(location.getCountryName());
    }

    /** Configure this trigger (expected to autowire in any dependencies) */
    @Override
    public void configure(Configurer configurer) {
        configurer.configure(this);
    }
}
