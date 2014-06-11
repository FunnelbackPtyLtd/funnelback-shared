package com.funnelback.publicui.curator.action;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.model.curator.config.Action;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.resource.impl.GroovyObjectResource;
import com.funnelback.springmvc.service.resource.ResourceManager;

/**
 * Perform an action which is specified by an external Groovy class.
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Component
public class GroovyAction implements Action {

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
    private GroovyActionInterface getActionImplementation(ApplicationContext context) {
        if (resourceManager == null) {
            resourceManager = context.getBean(ResourceManager.class);
        }

        GroovyActionInterface result;
        try {
            result = resourceManager.load(new GroovyObjectResource<GroovyActionInterface>(new File(classFile)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }
    
    /**
     * Performs the desired action (by calling performAction on an instance of
     * the specified Groovy class).
     */
    @Override
    public void performAction(SearchTransaction searchTransaction, Phase phase, ApplicationContext context) {
        getActionImplementation(context).performAction(searchTransaction, phase, properties);
    }

    /**
     * Checks whether the desired action should be run in the given phase (by
     * calling runsInPhase on an instance of the specified Groovy class).
     */
    @Override
    public boolean runsInPhase(Phase phase, ApplicationContext context) {
        return getActionImplementation(context).runsInPhase(phase, properties);
    }
}
