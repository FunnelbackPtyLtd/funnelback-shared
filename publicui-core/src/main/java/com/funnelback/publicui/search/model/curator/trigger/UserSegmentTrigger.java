package com.funnelback.publicui.search.model.curator.trigger;

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
 * <p>
 * A trigger which activates only when the current request originates user matching
 * some particular Predictive Segmenter constraint.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class UserSegmentTrigger implements Trigger {
    
    /**
     * The type of segment to check.
     * 
     * Available segments depend on the fields in the predictive segmenter database in use.
     */
    @Getter
    @Setter
    private String segmentType;

    /**
     * The 'value' to check for in the segment data (currently a substring check)
     */
    @Getter
    @Setter
    private String segmentValue;

    /**
     * The type of matching to be performed between the user's detected segment
     * (haystack) and the given segmentValue (needle).
     * 
     * Default setting is case sensitive substring matching.
     */
    @Getter
    @Setter
    private StringMatchType matchType = StringMatchType.SUBSTRING;

    /**
     * Check whether the given searchTransaction originates from a country
     * listed in targetCountries. If it does, return true, otherwise false.
     */
    @Override
    public boolean activatesOn(SearchTransaction searchTransaction) {
        throw new UnsupportedOperationException(
            "The publicui-core UserSegmentTrigger does not support activatesOn calls."
                + " Please ensure the implementation in publicui-web's "
                + " com.funnelback.publicui.curator.trigger package is available.");
    }

    /** Configure this trigger (expected to autowire in any dependencies) */
    @Override
    public void configure(Configurer configurer) {
        configurer.configure(this);
    }
}
