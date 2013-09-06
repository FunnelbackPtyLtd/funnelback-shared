package com.funnelback.publicui.recommender.compare;

import com.funnelback.publicui.recommender.Recommendation;

import java.util.Comparator;

/**
 * QIEComparator is used to compare {@link Recommendation}s based on their QIE score.
 * @author fcrimmins@funnelback.com
 */

public final class QIEComparator implements Comparator<Recommendation> {

    public int compare(Recommendation o1, Recommendation o2) {
        return Float.compare(o2.getQieScore(), o1.getQieScore());
    }
}