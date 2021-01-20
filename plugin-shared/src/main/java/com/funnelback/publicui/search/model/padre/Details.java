package com.funnelback.publicui.search.model.padre;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>Details (header) of a PADRE result packet</p>
 * 
 * <p>Contains information about the collection and the
 * PADRE version.</p>
 * 
 * @since 11.0
 */
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Details {

    /** Version of the PADRE query processor. */
    @Getter @Setter private String padreVersion;
    
    /** Size of the index. */
    @Getter @Setter private String collectionSize;
    
    /** Last updated date of the index. */
    @Getter @Setter private Date collectionUpdated;
    
}
