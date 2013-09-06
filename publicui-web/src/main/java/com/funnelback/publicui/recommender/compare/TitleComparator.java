package com.funnelback.publicui.recommender.compare;

import com.funnelback.publicui.recommender.Recommendation;

import java.util.Comparator;

/**
 * TitleComparator is used to compare {@link Recommendation}s based on their titles,
 * to allow them to be sorted lexicographically.
 * @author fcrimmins@funnelback.com
 */

public final class TitleComparator implements Comparator<Recommendation> {

    public int compare(Recommendation o1, Recommendation o2) {
        return o1.getTitle().compareTo(o2.getTitle());
    }
}