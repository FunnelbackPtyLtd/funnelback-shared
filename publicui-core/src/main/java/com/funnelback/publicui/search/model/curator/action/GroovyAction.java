package com.funnelback.publicui.search.model.curator.action;

import groovy.lang.GroovyClassLoader;
import lombok.EqualsAndHashCode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.funnelback.publicui.search.model.curator.config.Action;
import com.funnelback.publicui.search.model.curator.config.Configurer;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * Perform an action which is specified by an external Groovy class.
 */
@NoArgsConstructor
@ToString
@Component
@EqualsAndHashCode
public class GroovyAction implements Action {

    /**
     * File specifying the Groovy class which implements the desired action.
     * 
     * This class is expected to implement the
     * {@link com.funnelback.publicui.search.model.curator.action.GroovyActionInterface}
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
     * Convenience constructor to create a new GroovyAction with a script and properties.
     */
    public GroovyAction(String classFile, Map<String, Object> properties) {
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
    private GroovyActionInterface getActionImplementation() {
        File classFileObject = new File(classFile);
        if (cachedActionImplementation == null ||
            classFileObject.lastModified() != cachedActionImplementationLastModified) {
            try (GroovyClassLoader cl = new GroovyClassLoader()) {
                @SuppressWarnings("unchecked")
                Class<GroovyActionInterface> clazz = cl.parseClass(classFileObject);
                try {
                    cachedActionImplementation = clazz.newInstance();
                    cachedActionImplementationLastModified = classFileObject.lastModified();
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
        
        return cachedActionImplementation;
    }
    
    @JsonIgnore
    private GroovyActionInterface cachedActionImplementation;
    @JsonIgnore
    private long cachedActionImplementationLastModified;
    
    /**
     * Performs the desired action (by calling performAction on an instance of
     * the specified Groovy class).
     */
    @Override
    public void performAction(SearchTransaction searchTransaction, Phase phase) {
        getActionImplementation().performAction(searchTransaction, phase, properties);
    }

    /**
     * Checks whether the desired action should be run in the given phase (by
     * calling runsInPhase on an instance of the specified Groovy class).
     */
    @Override
    public boolean runsInPhase(Phase phase) {
        return getActionImplementation().runsInPhase(phase, properties);
    }

    /** Configure this action (expected to autowire in any dependencies) */
    @Override
    public void configure(Configurer configurer) {
        configurer.configure(this);
    }
}
