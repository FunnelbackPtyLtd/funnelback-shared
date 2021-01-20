package com.funnelback.publicui.search.model.padre;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <p>A tier bar</p>
 * 
 * <p>Results are grouped by tier bars depending of how
 * they match the query terms. There is usually one
 * tier bar for the fully matching results and anoter one
 * for the partially matching results.<p>
 * 
 * @since 11.0
 */
@AllArgsConstructor
@NoArgsConstructor
public class TierBar implements ResultType {
    
    /** Number of query terms matched by this tier bar */
    @Getter @Setter private int matched;
    
    /**
     * Total number of query terms, including the one matched
     * by this tier bar.
     */
    @Getter @Setter private int outOf;
    
    /**
     * Rank of the first search result contained within
     * this tier bar
     */
    @Getter @Setter private int firstRank;

    /**
     * Rank of the last search result contained within
     * this tier bar
     */
    @Getter @Setter private int lastRank;
    
    /**
     * <p>Date of the events for this tier bar when the <tt>-events</tt>
     * query processor option is set.</p>
     */
    @Getter @Setter private Date eventDate;
    
    public TierBar(int matched, int outOf, Date eventDate) {
        this.matched = matched;
        this.outOf = outOf;
        this.eventDate = eventDate;
    }
    
}
