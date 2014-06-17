package com.funnelback.publicui.curator;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.IOException;

import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.curator.action.GroovyActionInterface;
import com.funnelback.springmvc.service.resource.ResourceManager;

@Component
public class SimpleGroovyActionResourceManager implements GroovyActionResourceManager {

    @Autowired
    ResourceManager resourceManager;
    
    @Override
    public GroovyActionInterface getGroovyObject(File script) {
        try (GroovyClassLoader cl = new GroovyClassLoader()) {
            @SuppressWarnings("unchecked")
            Class<GroovyActionInterface> clazz = cl.parseClass(script);
            try {
                return clazz.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } catch (CompilationFailedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}