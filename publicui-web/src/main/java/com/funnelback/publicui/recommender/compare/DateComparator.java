package com.funnelback.publicui.recommender.compare;

import com.funnelback.publicui.recommender.Recommendation;
import org.apache.log4j.Logger;

import java.util.Comparator;
import java.util.Date;

/**
 * DateComparator is used to compare {@link Recommendation}s based on their dates,
 * to allow them to be sorted.
 * @author fcrimmins@funnelback.com
 */

public final class DateComparator implements Comparator<Recommendation> {
    private static final Logger logger = Logger.getLogger(DateComparator.class);

    public int compare(Recommendation o1, Recommendation o2) {
        Date date1 = o1.getDate();
        Date date2 = o2.getDate();

        if (date1 == null || date2 == null) {
            logger.warn("No date available for one or more of: " + o1 + " " + o2);
            return Integer.MIN_VALUE;
        }

        return date2.compareTo(date1);
    }
}