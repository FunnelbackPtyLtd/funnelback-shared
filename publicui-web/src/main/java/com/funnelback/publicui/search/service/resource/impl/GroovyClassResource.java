package com.funnelback.publicui.search.service.resource.impl;


import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.IOException;

import lombok.extern.log4j.Log4j2;

import com.funnelback.common.filter.GroovyClassLoaderCache;

/**
 * Loads and compiles a Groovy class of type T
 * 
 */
@Log4j2
public class GroovyClassResource<T> extends GroovyFileResource<Class<T>> {
    
    private final String collection;
    private final File searchHome;
    
    GroovyFileResource<Class<T>> groovyFileResource;
    
    public GroovyClassResource(File file, String collection, File searchHome) {
        super(file);
        this.collection = collection;
        this.searchHome = searchHome;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> parse() throws IOException {
        try (GroovyClassLoader cl = GroovyClassLoaderCache.makeNewGroovyClassLoader(searchHome, collection)) {
            return cl.parseClass(file);
        }
    }

}
