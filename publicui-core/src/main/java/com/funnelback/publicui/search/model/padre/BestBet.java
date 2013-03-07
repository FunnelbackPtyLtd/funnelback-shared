package com.funnelback.publicui.search.model.padre;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * A best bet
 * 
 * @since 11.0
 * @see <code>best_bets.cfg</code>
 */
@RequiredArgsConstructor
@AllArgsConstructor
public class BestBet {

    /** Query or regular expression that triggers the best bet. */
    @Getter @Setter private String trigger;
    
    /** Link to the best bet */
    @Getter @Setter private String link;
    
    /** Title of the best bet*/
    @Getter @Setter private String title;
    
    /** Description of the best bet*/
    @Getter @Setter private String description;
    
    /** URL to the best bet with click tracking (<code>click.cgi</code>) */
    @Getter @Setter private String clickTrackingUrl;
    
    /**
     * Custom data placeholder allowing any arbitrary data to be
     * stored by hook scripts.
     */
    @Getter private final Map<String, Object> customData = new HashMap<String, Object>();
    
    /** Constants for the PADRE XML result packet tags. */
    public static class Schema {
        public static final String BB = "bb";
        
        public static final String BB_TRIGGER = "bb_trigger";
        public static final String BB_LINK = "bb_link";
        public static final String BB_TITLE = "bb_title";
        public static final String BB_DESC = "bb_desc";    
    }
    
}
