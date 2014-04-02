package com.funnelback.publicui.search.lifecycle.input.processors;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.input.AbstractInputProcessor;
import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.model.curator.config.Action.Phase;
import com.funnelback.publicui.search.model.curator.config.CuratorConfig;
import com.funnelback.publicui.search.model.curator.config.TriggerActions;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * <p>
 * The curator input processor performs curator actions which should occur
 * before the padre request is executed (such as asking padre to promote or
 * remove URLs).
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
 * 
 */
@Component("curatorInputProcessor")
public class Curator extends AbstractInputProcessor {

    /**
     * The Spring application context to provide to triggers and actions which
     * might require it.
     */
    @Autowired
    @Setter
    private ApplicationContext context;

    /**
     * Find all curator actions (in the current profile's CuratorConfig) which
     * are triggered on input for this searchTranscation, and perform them in
     * turn.
     */
    @Override
    public void processInput(SearchTransaction searchTransaction) throws InputProcessorException {
        if (SearchTransactionUtils.hasQueryAndCollection(searchTransaction)) {
            if (CuratorConfig.isCuratorActive(searchTransaction)) {

                String profileName = searchTransaction.getQuestion().getProfile();

                if (searchTransaction.getQuestion().getCollection().getProfiles().containsKey(profileName)) {
                    CuratorConfig config = searchTransaction.getQuestion().getCollection().getProfiles()
                        .get(profileName).getCuratorConfig();

                    for (TriggerActions ta : config.getTriggerActions()) {
                        if (ta.getActions().hasActionForPhase(Phase.INPUT, context) && ta.getTrigger().activatesOn(searchTransaction, context)) {
                            ta.getActions().performActions(searchTransaction, Phase.INPUT, context);
                        }
                    }
                }
            }
        }
    }

}
