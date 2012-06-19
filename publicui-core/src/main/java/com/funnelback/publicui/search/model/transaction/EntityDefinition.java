package com.funnelback.publicui.search.model.transaction;

/**
 * An entity, its definition and the URL it came from.
 * @author francis
 * @since 11.0
 */
public class EntityDefinition {
	/** An entity e.g The ABC. */
    private String entity;
    
    /** A definition e.g. "The Australian Broadcasting Corporation" */
    private String definition;
    
    /** A URL that the entity and definition came from e.g. http://www.abc.net.au/ */
    private String url;
    
    public EntityDefinition(String entity, String definition, String URL) {
        this.entity = entity;
        this.definition = definition;
        this.url = URL;
    }
    
    public void setEntity(String value) {
        entity = value;	
    }
    
    public String getEntity() {
    	return entity;
    }
    
    public void setDefinition(String value) {
        definition = value;	
    }
    
    public String getDefinition() {
    	return definition;
    }
    
    public void setUrl(String value) {
        url = value;	
    }
    
    public String getUrl() {
    	return url;
    }
}
