package com.funnelback.publicui.search.lifecycle.output.processors;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.curator.config.Action.Phase;
import com.funnelback.publicui.search.model.curator.config.CuratorConfig;
import com.funnelback.publicui.search.model.curator.config.TriggerActions;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * <p>
 * The curator output processor performs curator actions which should occur
 * after the padre query has been executed (such as adding things to the data
 * model).
 * </p>
 * 
 * <p>
 * The actions to run are based on the content of the curator.yaml file within
 * the profile being queried and the matching (or not) of the triggers defined
 * for each set of actions.
 * </p>
 * 
 * <p>
 * The curator actions can be disabled as a whole by setting curator=false as a
 * URL parameter.
 * </p>
 */
@Component("curatorOutputProcessor")
public class Curator extends AbstractOutputProcessor {

    /**
     * The Spring application context to provide to triggers and actions which
     * might require it.
     */
    @Autowired
    @Setter
    private ApplicationContext context;

    
    /**
     * Find all curator actions (in the current profile's CuratorConfig) which
     * are triggered on output for this searchTranscation, and perform them in
     * turn.
     */
    @Override
    public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
        com.funnelback.publicui.search.model.curator.Curator model = new com.funnelback.publicui.search.model.curator.Curator();
        searchTransaction.getResponse().setCurator(model);

        if (SearchTransactionUtils.hasQueryAndCollection(searchTransaction)) {

            if (CuratorConfig.isCuratorActive(searchTransaction)) {

                String profileName = searchTransaction.getQuestion().getProfile();

                if (searchTransaction.getQuestion().getCollection().getProfiles().containsKey(profileName)) {
                    CuratorConfig config = searchTransaction.getQuestion().getCollection().getProfiles()
                        .get(profileName).getCuratorConfig();

                    for (TriggerActions ta : config.getTriggerActions()) {
                        if (ta.getActions().hasActionForPhase(Phase.OUTPUT) && ta.getTrigger().activatesOn(searchTransaction)) {
                            ta.getActions().performActions(searchTransaction, Phase.OUTPUT);
                        }
                    }
                }
            }
        }

    }
}
