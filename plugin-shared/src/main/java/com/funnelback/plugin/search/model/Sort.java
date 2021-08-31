package com.funnelback.plugin.search.model;

import lombok.RequiredArgsConstructor;

/** Sort options for returned suggestions */
@RequiredArgsConstructor
public enum Sort {
    /** Sort by descending weight */
    DescendingWeight(0),
    /** Sort by ascending length */
    AscendingLength(1),
    /** Sort by ascending alphabetic order */
    AscendingAlphabetic(2),
    /** Sort by weighted combo of weight and length
     * score = alpha * weight + (1 - alpha) * length_score
     */
    DescendingWeightedComboScore(3);

    /** Sort code for <code>libqs</code> */
    public final int code;
}
