package com.funnelback.publicui.search.service.resource.impl;


import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.IOException;

import lombok.extern.log4j.Log4j;

/**
 * Loads, compiles and instantiates a Groovy class (with a no-arg constructor).
 * 
 * Note that any underlying Groovy object should be reusable and may need to be
 * thread-safe if it is to be used in a multi-threaded environment.
 */
@Log4j
public class GroovyObjectResource<T> extends AbstractSingleFileResource<T> {
    
    /**
     * @param file The file representing the groovy class to instantiate
     */
    public GroovyObjectResource(File file) {
        super(file);
    }

    /**
     * Compile the defined file into a class, create an instance (calling a
     * no-arg constructor) and then return the created instance.
     */
    @SuppressWarnings("unchecked")
    @Override
    public T parse() throws IOException {
        log.debug("Creating object form Groovy class defined at '"+file.getAbsolutePath()+"'");
        try (GroovyClassLoader cl = new GroovyClassLoader()) {
            Class<T> clazz = cl.parseClass(file);
            try {
                return clazz.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
