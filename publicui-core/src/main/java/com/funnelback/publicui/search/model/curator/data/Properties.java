package com.funnelback.publicui.search.model.curator.data;

import java.util.HashMap;
import java.util.Map;

import com.funnelback.publicui.search.model.curator.Exhibit;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Properties represents arbitrary structured data added by the curator system
 * to be displayed within the search results.
 * </p>
 * 
 * <p>
 * May be most useful where structured data is needed, but neither Message nor
 * UrlAdvert are appropriate.
 * </p>
 */
public class Properties implements Exhibit {

    /**
     * The data for the Properties object.
     */
    @Getter
    @Setter
    private Map<String, String> properties = new HashMap<String, String>();

    /**
     * A category for the properties which may be used by an ftl file to display
     * different types of properties in different ways.
     */
    @Getter
    @Setter
    private String category;

}
