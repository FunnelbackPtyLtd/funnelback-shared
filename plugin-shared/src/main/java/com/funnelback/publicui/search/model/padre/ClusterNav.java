package com.funnelback.publicui.search.model.padre;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <p>Contextual navigation details to navigate through
 * results and clusters.</p>
 * 
 * <p>
 * For example you can follow a navigation path like:</p>
 * <ul>
 *     <li>King</li>
 *  <li>King Richard</li>
 *  <li>King Richard the great</li>
 * </ul>
 * <p>
 * For each step a {@link ClusterNav} item will be provided.
 * </p>
 * 
 * @since 11.0
 * @see ContextualNavigation
 * @see Category
 * @see Cluster
 */
@AllArgsConstructor
@NoArgsConstructor
public class ClusterNav {

    /**
     * <p>Level of this navigation item.</p>
     * <p>0 is used for the root, then 1, 2, etc.</p>
     */
    @Getter @Setter private Integer level;
    
    /**
     * <p>URL to reach this navigation item.</p>
     */
    @Getter @Setter private String url;
    
    /**
     * Label for this navigation item, such as "King Richard".
     */
    @Getter @Setter private String label;
}
