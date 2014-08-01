package com.funnelback.publicui.search.model.curator.trigger;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.model.curator.config.Configurer;
import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;


/**
 * <p>
 * A trigger which activates only when the current query contains all the words
 * given as triggers.
 * </p>
 * 
 * <p>
 * The query may contain other words without affecting the trigger, and the
 * order of the words within the query is not considered important.
 * </p>
 */
@NoArgsConstructor
@ToString
@Component
public class GroovyTrigger implements Trigger {

    /**
     * File specifying the Groovy class which implements the desired action.
     * 
     * This class is expected to implement the
     * {@link com.funnelback.publicui.curator.action.GroovyActionInterface}
     * interface.
     */
    @Getter
    @Setter
    private String classFile;

    /**
     * Any properties associated with the action (passed to the performAction
     * and runsInPhase methods).
     */
    @Getter
    @Setter
    private Map<String, Object> properties = new HashMap<String, Object>();

    /**
     * Convenience constructor to create a new GroovyTrigger with a script and properties.
     */
    public GroovyTrigger(String classFile, Map<String, Object> properties) {
        this.classFile = classFile;
        this.properties = properties;
    }

    /**
     * Returns the GroovyTriggerInterface implementation for this trigger.
     * 
     * The returned implementation is cached, however the original file has a modification
     * timestamp check performed on every request (and the implementation refreshed if the
     * file is changed).
     */
    @JsonIgnore
    public GroovyTriggerInterface getTriggerImplementation() {
        File classFileObject = new File(classFile);
        if (cachedTriggerImplementation == null ||
            classFileObject.lastModified() != cachedTriggerImplementationLastModified) {
            try (GroovyClassLoader cl = new GroovyClassLoader()) {
                @SuppressWarnings("unchecked")
                Class<GroovyTriggerInterface> clazz = cl.parseClass(classFileObject);
                try {
                    cachedTriggerImplementation = clazz.newInstance();
                    cachedTriggerImplementationLastModified = classFileObject.lastModified();
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
        
        return cachedTriggerImplementation;
    }
    @JsonIgnore
    private GroovyTriggerInterface cachedTriggerImplementation;
    @JsonIgnore
    private long cachedTriggerImplementationLastModified;
    
    /**
     * Check the given searchTransaction to see if the trigger activates on this request.
     */
    @Override
    public boolean activatesOn(SearchTransaction searchTransaction) {
        return getTriggerImplementation().activatesOn(searchTransaction, properties);
    }

    /** Configure this trigger (expected to autowire in any dependencies) */
    @Override
    public void configure(Configurer configurer) {
        configurer.configure(this);
    }
}
