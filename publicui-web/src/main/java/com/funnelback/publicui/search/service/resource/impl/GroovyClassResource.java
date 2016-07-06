package com.funnelback.publicui.search.service.resource.impl;


import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.IOException;

import lombok.extern.log4j.Log4j2;

import com.funnelback.common.filter.GroovyClassLoaderCache;
import com.funnelback.springmvc.service.resource.impl.AbstractSingleFileResource;

/**
 * Loads and compiles a Groovy class of type T
 * 
 */
@Log4j2
public class GroovyClassResource<T> extends AbstractSingleFileResource<Class<T>> {
    
    private final String collection;
    private final File searchHome;
   
    /**
     * As the dependencies of groovy script can change without us knowing
     * we don't keep groovy scripts for more than 60s this way at least 
     * every 60s classes this script might use are reloaded.
     */
    static final long MAX_SCRIPT_AGE = 60*1000;
    
    public GroovyClassResource(File file, String collection, File searchHome) {
        super(file);
        this.collection = collection;
        this.searchHome = searchHome;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> parse() throws IOException {
        log.debug("Loading Groovy script from '"+file.getAbsolutePath()+"'");
        
        try (GroovyClassLoader cl = GroovyClassLoaderCache.makeNewGroovyClassLoader(searchHome, collection)) {
            return cl.parseClass(file);
        }
    }
    
    
    @Override
    public boolean isStale(long timestamp, Class<T> t) {
        if(isScriptTooOld(timestamp)) {
            return true;
        }
        return super.isStale(timestamp, t);
    }
    
    
    long getTime() {
        return System.currentTimeMillis();
    }
    
    
    boolean isScriptTooOld(long time) {
        if(getTime() - time > MAX_SCRIPT_AGE) {
            return true;
        }
        return false;
    }

}
