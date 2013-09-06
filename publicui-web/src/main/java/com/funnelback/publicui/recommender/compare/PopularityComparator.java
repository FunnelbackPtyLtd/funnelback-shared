package com.funnelback.publicui.recommender.compare;

import com.funnelback.publicui.recommender.Recommendation;

import java.util.Comparator;

/**
 * PopularityComparator is used to compare {@link Recommendation}s based on their popularity,
 * to allow them to be sorted.
 * @author fcrimmins@funnelback.com
 */

public final class PopularityComparator implements Comparator<Recommendation> {

    public int compare(Recommendation o1, Recommendation o2) {
        return Long.compare(o2.getPopularity(), o1.getPopularity());
    }
}