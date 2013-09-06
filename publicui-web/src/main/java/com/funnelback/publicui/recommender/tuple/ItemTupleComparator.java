package com.funnelback.publicui.recommender.tuple;

import java.util.Comparator;

/**
 * ItemTupleComparator is used to compare ItemTuple objects based on their score.
 * @author fcrimmins@funnelback.com
 */

public final class ItemTupleComparator implements Comparator<ItemTuple> {

    public int compare(ItemTuple s1, ItemTuple s2) {
        return Double.compare(s2.getScore(), s1.getScore());
    }
}