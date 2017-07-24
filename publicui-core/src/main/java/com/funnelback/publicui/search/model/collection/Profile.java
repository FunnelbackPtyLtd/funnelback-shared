package com.funnelback.publicui.search.model.collection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.funnelback.config.configtypes.service.ServiceConfigReadOnly;
import com.funnelback.publicui.search.model.curator.config.CuratorConfig;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <p>A search profile.</p>
 * 
 * <p>Profile configurations resides in a sub directory
 * in the main collection's <code>conf/[collection]/</code> directory.<p>
 * 
 * <p>Each profile can have specific query processor options and a specific
 * faceted navigation configuration.</p>
 * 
 * <p>Each collection comes with two default profiles, <code>_default</code> and
 * <code>_default_preview</code> used in the preview / publish system.
 * 
 * @since 11.0
 */
@JsonIgnoreProperties("curatorConfig")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class Profile {

    /**
     * Profile id, identical to the name of the folder containing the
     * profile configuration under <code>conf/[collection]/[profile]/</code>.
     */
    @Getter private String id;
    
    /**
     * Faceted navigation configuration in
     * <code>conf/[collection]/[profile]/faceted_navigation.cfg</code>
     */
    @Getter @Setter private FacetedNavigationConfig facetedNavConfConfig;

    /** Curator configuration in <code>conf/[collection]/[profile]/curator.json</code>
     * or <code>conf/[collection]/[profile]/curator.yaml</code>. JSON takes precedence. */
    @XStreamOmitField
    @Getter @Setter private CuratorConfig curatorConfig = new CuratorConfig();

    @XStreamOmitField
    @Getter @Setter private ServiceConfigReadOnly serviceConfig;

    /**
     * <p>Specific query processor options for this profile.</p>
     * 
     * <p>Read from <code>conf/[collection]/[profile]/padre_opts.cfg</code>.</p>
     */
    @Getter @Setter private String padreOpts;
    
    public Profile(String id) {
        this.id = id;
    }
    
    /**
     * A Profile builder with the currently set fields already set on the builder.
     *  
     * @return
     */
    public ProfileBuilder cloneBuilder() {
        return Profile.builder()
            .id(getId())
            .facetedNavConfConfig(getFacetedNavConfConfig())
            .curatorConfig(getCuratorConfig())
            .padreOpts(getPadreOpts())
            .serviceConfig(getServiceConfig());
    }
    
}
