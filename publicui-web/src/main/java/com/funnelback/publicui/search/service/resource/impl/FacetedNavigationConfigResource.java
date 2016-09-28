package com.funnelback.publicui.search.service.resource.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.funnelback.publicui.search.model.collection.FacetedNavigationConfig;
import com.funnelback.publicui.xml.FacetedNavigationConfigParser;
import com.funnelback.publicui.xml.FacetedNavigationConfigParser.FacetedNavigationConfigParseException;
import com.funnelback.publicui.xml.FacetedNavigationConfigParser.Facets;
import com.funnelback.springmvc.service.resource.impl.AbstractSingleFileResource;

import lombok.extern.log4j.Log4j2;

/**
 * Loads and parse a faceted navigation configuration file.
 */
@Log4j2
public class FacetedNavigationConfigResource extends AbstractSingleFileResource<FacetedNavigationConfig> {

    private final FacetedNavigationConfigParser fnConfigParser;
    
    public FacetedNavigationConfigResource(File file, FacetedNavigationConfigParser fnConfigParser) {
        super(file);
        this.fnConfigParser = fnConfigParser;
    }
    
    public FacetedNavigationConfig parse() throws IOException {
        log.debug("Loading faceted navigation config from '"+file.getAbsolutePath()+"'");
        try {
            Facets f = fnConfigParser
                    .parseFacetedNavigationConfiguration(FileUtils
                            .readFileToByteArray(file));
            return new FacetedNavigationConfig(f.facetDefinitions);
        } catch (FacetedNavigationConfigParseException xpe) { //TODO better checked exception
            log.error(
                    "Error while parsing faceted navigation configuration from '"
                            + file.getAbsolutePath() + "'", xpe);
            return null;
        }
    }

}
