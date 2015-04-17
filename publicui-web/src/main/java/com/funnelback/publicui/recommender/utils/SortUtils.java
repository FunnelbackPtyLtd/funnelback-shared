package com.funnelback.publicui.recommender.utils;

import com.funnelback.reporting.recommender.tuple.ItemTuple;
import com.funnelback.reporting.recommender.tuple.ItemTupleComparator;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import lombok.extern.log4j.Log4j2;

/**
 * Sorting utilities.
 * @author fcrimmins@funnelback.com
 */
@Log4j2
public final class SortUtils {

    // Private constructor to avoid unnecessary instantiation of the class
    private SortUtils() {
    }

    /**
     * Return a sorted version of the given list, by sorting on multiple fields in
     * the order: frequency, source, rank.
     * @param input list of ItemTuples (recommendations)
     * @return sorted version of the input list, with no duplicate items.
     */
    public static List<ItemTuple> sortList(List<ItemTuple> input) {
        Date startTime = new Date();
        int originalSize = input.size();

        Multiset<ItemTuple> tupleMultiset = LinkedHashMultiset.create();
        tupleMultiset.addAll(input);

        for (Multiset.Entry<ItemTuple> entry : tupleMultiset.entrySet()) {
            ItemTuple itemTuple = entry.getElement();
            int count = entry.getCount();

            if (count > 1) {
                // Increment the existing frequency by the number of additional times it appears in the MultiSet
                int frequency = itemTuple.getFrequency() + (count - 1);
                itemTuple.setFrequency(frequency);

                // Set the number of occurrences of this element in the MultiSet back down to one
                tupleMultiset.setCount(itemTuple, 1);
            }
        }

        // Now that we have updated the frequency values, sort the list and return it
        List<ItemTuple> output = Arrays.asList(tupleMultiset.toArray(new ItemTuple[tupleMultiset.size()]));
        Collections.sort(output, new ItemTupleComparator());

        long timeTaken = System.currentTimeMillis() - startTime.getTime();
        log.debug("Sorted list of size " + originalSize + " in " + timeTaken + "ms");

        return output;
    }
}
