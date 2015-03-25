package com.funnelback.publicui.search.model.padre;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A date count (Used in faceted navigation).
 * 
 * @since v12.0
 */
@AllArgsConstructor
@NoArgsConstructor
public class DateCount {

    /**
     * <p>Date constraint to apply to select documents
     * for this date count, e.g. <code>|d&gt;1jan2003</code>.</p>
     */
    @Getter @Setter
    private String queryTerm;
    
    /**
     * Number of documents for this date.
     */
    @Getter @Setter
    private int count;
    
}
