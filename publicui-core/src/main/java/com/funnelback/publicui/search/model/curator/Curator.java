package com.funnelback.publicui.search.model.curator;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * A Curator will contain and additional information (exhibits) which has
 * been added to the result packet by curator actions.
 * 
 * @since 13.0
 */
public class Curator {

    /**
     * Contains the list of additional elements (exhibits) which curator actions
     * have caused to be available for display within the search result page.
     */
    @Getter @Setter
    private List<Exhibit> exhibits = new ArrayList<Exhibit>();

}
