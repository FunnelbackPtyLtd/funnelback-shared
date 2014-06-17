package com.funnelback.publicui.curator.trigger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.curator.GroovyTriggerResourceManager;
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
@AllArgsConstructor
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
     * A resource manager object which handles loading of groovy objects
     */
    @Setter
    private GroovyTriggerResourceManager groovyTriggerResourceManager;

    /**
     * Check the given searchTransaction to see if the trigger activates on this request.
     */
    @Override
    public boolean activatesOn(SearchTransaction searchTransaction) {
        return groovyTriggerResourceManager.getGroovyObject(new File(classFile)).activatesOn(searchTransaction, properties);
    }

}
