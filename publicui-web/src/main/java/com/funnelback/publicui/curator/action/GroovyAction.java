package com.funnelback.publicui.curator.action;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.publicui.search.model.curator.config.Action;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.resource.ResourceManager;
import com.funnelback.publicui.search.service.resource.impl.GroovyObjectResource;

/**
 * Perform an action which is specified by an external Groovy class.
 */
@AllArgsConstructor
@NoArgsConstructor
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
     * Resource manager used to reload the Groovy class when it changes
     */
    @Autowired
    @Setter
    protected ResourceManager resourceManager;
    
    /**
     * Any properties associated with the action (passed to the performAction
     * and runsInPhase methods).
     */
    @Getter
    @Setter
    private Map<String, Object> properties = new HashMap<String, Object>();

    /**
     * Loads the GroovyActionInterface implementation from the resourceManager (which will cache it between
     * requests until the file changes) and returns it for use.
     */
    private GroovyActionInterface getActionImplementation() {
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
}
