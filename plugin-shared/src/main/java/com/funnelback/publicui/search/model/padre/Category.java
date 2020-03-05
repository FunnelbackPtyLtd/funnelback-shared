package com.funnelback.publicui.search.model.padre;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A contextual navigation category (type, topic, site).
 * 
 * @since 11.0
 * @see ContextualNavigation
 * @see Cluster
 * @see ClusterNav
 */
@AllArgsConstructor
@NoArgsConstructor
public class Category {

    /**
     * Name of the category ("type", "topic" or "site").
     */
    @Getter @Setter private String name;
    
    /**
     * Number of additional values of this categories,
     * if any.
     */
    @Getter @Setter private Integer more;
    
    /** Link to get more values for this category. */
    @Getter @Setter private String moreLink;
    /** Link to get fewer values for this category. */
    @Getter @Setter private String fewerLink;
    
    /** List of <em>clusters</em> (suggestions) for this category. */
    @Getter private final List<Cluster> clusters = new ArrayList<Cluster>();
    
    public Category(String name, Integer more) {
        this.name = name;
        this.more = more;
    }
}
