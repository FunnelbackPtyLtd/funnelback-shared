package com.funnelback.publicui.curator.trigger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.resource.impl.GroovyObjectResource;
import com.funnelback.springmvc.service.resource.ResourceManager;


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
@AllArgsConstructor
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
     * Resource manager used to reload the Groovy class when it changes
     */
    @Setter
    private ResourceManager resourceManager;
    
    /**
     * Loads the GroovyActionInterface implementation from the resourceManager (which will cache it between
     * requests until the file changes) and returns it for use.
     */
    private GroovyTriggerInterface getTriggerImplementation(ApplicationContext context) {
        if (resourceManager == null) {
            resourceManager = context.getBean(ResourceManager.class);
        }
        
        GroovyTriggerInterface result;
        try {
            result = resourceManager.load(new GroovyObjectResource<GroovyTriggerInterface>(new File(classFile)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (result == null) {
            throw new RuntimeException(new ClassNotFoundException("No class was loaded from " + classFile));
        }

        return result;
    }

    /**
     * Check the given searchTransaction to see if the trigger activates on this request.
     */
    @Override
    public boolean activatesOn(SearchTransaction searchTransaction, ApplicationContext context) {
        return getTriggerImplementation(context).activatesOn(searchTransaction, properties);
    }

}
