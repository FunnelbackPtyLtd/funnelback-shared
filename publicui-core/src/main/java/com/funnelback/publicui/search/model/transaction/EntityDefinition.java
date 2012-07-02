package com.funnelback.publicui.search.model.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * An text mined entity, its definition and the URL it came from.
 * 
 * @since 12.0
 */
@AllArgsConstructor
public class EntityDefinition {
	
	/** An entity e.g <em>"The ABC"</em>. */
    @Getter @Setter private String entity;
    
    /** A definition e.g. <em>"The Australian Broadcasting Corporation"</em> */
    @Getter @Setter private String definition;
    
    /** A URL that the entity and definition came from e.g. <em>http://www.abc.net.au/</em< */
    @Getter @Setter private String url;
    
}
