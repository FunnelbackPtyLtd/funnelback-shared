package com.funnelback.publicui.search.model.curator.data;

import java.util.HashMap;
import java.util.Map;

import com.funnelback.publicui.search.model.curator.Exhibit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>
 * Properties represents arbitrary structured data added by the curator system
 * to be displayed within the search results.
 * </p>
 * 
 * <p>
 * May be most useful where structured data is needed, but neither {@link Message} nor
 * {@link UrlAdvert} are appropriate.
 * </p>
 * 
 * @since 13.0
 */
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Properties implements Exhibit {

    /**
     * The data for the Properties object.
     */
    @Getter
    @Setter
    private Map<String, Object> properties = new HashMap<String, Object>();

    /**
     * A category for the properties which may be used by a FreeMarker template to display
     * different types of properties in different ways.
     */
    @Getter
    @Setter
    private String category;

}
