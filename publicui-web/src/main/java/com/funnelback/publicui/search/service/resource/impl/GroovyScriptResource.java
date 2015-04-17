package com.funnelback.publicui.search.service.resource.impl;


import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;

import java.io.File;
import java.io.IOException;

import com.funnelback.springmvc.service.resource.impl.AbstractSingleFileResource;

import lombok.extern.log4j.Log4j2;

/**
 * Loads and compiles a Groovy script
 */
@Log4j2
public class GroovyScriptResource extends AbstractSingleFileResource<Class<Script>> {
    
    public GroovyScriptResource(File file) {
        super(file);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<Script> parse() throws IOException {
        log.debug("Loading Groovy script from '"+file.getAbsolutePath()+"'");
        try (GroovyClassLoader cl = new GroovyClassLoader()) {
            return cl.parseClass(file);
        }
    }

}
