package com.funnelback.publicui.search.model.padre;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <p>Top n results for an RMC item count (Results that would
 * be returned is the RMC constraint is applied)</p>
 * 
 * <p>Only present when the corresponding query processor option
 * is set.</p>
 * 
 * @since 11.2
 */
@AllArgsConstructor
@NoArgsConstructor
public class RMCItemResult {

    /** Title of the result */
    @Getter @Setter private String title;
    
    /** URL to access the search result. */
    @Getter @Setter private String liveUrl;
    
    /** Snippet (non query-biased) */
    @Getter @Setter private String summary;
}
