package com.funnelback.publicui.search.service.resource.impl;


import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;

import java.io.File;
import java.io.IOException;

import lombok.extern.log4j.Log4j2;

import com.funnelback.common.filter.GroovyClassLoaderCache;

/**
 * Loads and compiles a Groovy script
 */
@Log4j2
public class GroovyScriptResource extends GroovyFileResource<Class<Script>> {
    
    private final String collection;
    private final File searchHome;
    
    public GroovyScriptResource(File file, String collection, File searchHome) {
        super(file);
        this.collection = collection;
        this.searchHome = searchHome;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<Script> parse() throws IOException {
        log.debug("Loading Groovy script from '"+file.getAbsolutePath()+"'");
        
        try (GroovyClassLoader cl = GroovyClassLoaderCache.makeNewGroovyClassLoader(searchHome, collection)) {
            return cl.parseClass(file);
        }
    }

}
