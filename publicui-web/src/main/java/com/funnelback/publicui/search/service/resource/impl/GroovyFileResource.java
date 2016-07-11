package com.funnelback.publicui.search.service.resource.impl;


import java.io.File;

import lombok.extern.log4j.Log4j2;

import com.funnelback.springmvc.service.resource.impl.AbstractSingleFileResource;

@Log4j2
abstract class GroovyFileResource<T> extends AbstractSingleFileResource<T> {

    /**
     * As the dependencies of groovy script can change without us knowing
     * we don't keep groovy scripts for more than 60s this way at least 
     * every 60s classes this script might use are reloaded.
     */
    static final long MAX_SCRIPT_AGE = 60*1000;
    
    public GroovyFileResource(File file) {
        super(file);
    }
    
    @Override
    public boolean isStale(long timestamp, T t) {
        log.fatal("Is it stale?");
        if(isScriptTooOld(timestamp)) {
            return true;
        }
        return super.isStale(timestamp, t);
    }
    
    
    long getTime() {
        return System.currentTimeMillis();
    }
    
    
    boolean isScriptTooOld(long time) {
        log.fatal(getTime() +  " - "  + time + " >"  + MAX_SCRIPT_AGE);
        if(getTime() - time > MAX_SCRIPT_AGE) {
            log.fatal("TOO old!");
            return true;
        }
        log.fatal("its ok!");
        return false;
    }

}
