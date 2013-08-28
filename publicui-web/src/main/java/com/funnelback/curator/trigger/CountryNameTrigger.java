package com.funnelback.curator.trigger;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.publicui.search.model.geolocation.Location;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * A trigger which activates only when the current request originates from a
 * country whose name exists in the targetCountries list.
 * 
 * The detection of the originating country requires geolocation to be active
 * (otherwise this trigger will never activate).
 */
@AllArgsConstructor
@NoArgsConstructor
public class CountryNameTrigger implements Trigger {

    /**
     * The list of country names (case sensitive) for which this trigger should
     * activate.
     */
    @Getter
    @Setter
    private Set<String> targetCounties = new HashSet<String>();

    /**
     * Check whether the given searchTransaction originates from a country
     * listed in targetCountries. If it does, return true, otherwise false.
     */
    @Override
    public boolean activatesOn(SearchTransaction searchTransaction) {
        Location location = searchTransaction.getQuestion().getLocation();
        return targetCounties.contains(location.getCountryName());
    }

}
