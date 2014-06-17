package com.funnelback.publicui.curator.action;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.curator.GroovyActionResourceManager;
import com.funnelback.publicui.search.model.curator.config.Action;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

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
     * A resource manager object which handles loading of groovy objects
     */
    @Setter
    private GroovyActionResourceManager groovyActionResourceManager;
    
    /**
     * Performs the desired action (by calling performAction on an instance of
     * the specified Groovy class).
     */
    @Override
    public void performAction(SearchTransaction searchTransaction, Phase phase) {
        groovyActionResourceManager.getGroovyObject(new File(classFile)).performAction(searchTransaction, phase, properties);
    }

    /**
     * Checks whether the desired action should be run in the given phase (by
     * calling runsInPhase on an instance of the specified Groovy class).
     */
    @Override
    public boolean runsInPhase(Phase phase) {
        return groovyActionResourceManager.getGroovyObject(new File(classFile)).runsInPhase(phase, properties);
    }
}
