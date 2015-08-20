package com.funnelback.publicui.search.model.curator.trigger;

import lombok.EqualsAndHashCode;

import java.util.Date;

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
 * A trigger which activates only when the current date and time falls after the
 * given start date but before the given end date.
 * </p>
 * 
 * <p>
 * If the start/end date is set to null then it will not be considered as a
 * constraint, allowing a trigger to end on a given date or start on a given
 * date without the need to specify the other boundary. If both boundaries are
 * left as null then the trigger will always activate.
 * </p>
 * 
 * <p>
 * The date which is compared is based on the server's current time, and the
 * start and end dates are assumed to be in the same timezone as the server.
 * </p>
 * 
 * <p>
 * The after/before checks are exclusive (i.e. the specified boundary is not
 * included in the active period), however the resolution has millisecond
 * precision, so in practice it would be hard to observe the difference between
 * this and the opposite (inclusive) behavior.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class DateRangeTrigger implements Trigger {

    /**
     * The date before which the trigger will never activate. If left as null
     * then no start date constraint is applied.
     */
    @Getter
    @Setter
    private Date startDate = null;

    /**
     * The date after which the trigger will never activate. If left as null
     * then no end date constraint is applied.
     */
    @Getter
    @Setter
    private Date endDate = null;

    /**
     * Check whether the server's current time falls between the startDate and
     * endDate, and return true if it does and false otherwise. If a start/end
     * constraint is null then any date is considered permitted on that
     * boundary.
     */
    @Override
    public boolean activatesOn(SearchTransaction searchTransaction) {
        Date currentTime = new Date();

        if ((startDate == null || currentTime.after(startDate)) && (endDate == null || currentTime.before(endDate))) {
            return true;
        }
        return false;
    }

    /** Configure this trigger (expected to autowire in any dependencies) */
    @Override
    public void configure(Configurer configurer) {
        configurer.configure(this);
    }
}
