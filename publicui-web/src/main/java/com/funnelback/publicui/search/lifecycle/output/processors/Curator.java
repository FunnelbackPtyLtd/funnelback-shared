package com.funnelback.publicui.search.lifecycle.output.processors;

import java.util.Map.Entry;

import org.springframework.stereotype.Component;

import com.funnelback.publicui.search.lifecycle.output.AbstractOutputProcessor;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.curator.CuratorModel;
import com.funnelback.publicui.search.model.curator.config.Action.Phase;
import com.funnelback.publicui.search.model.curator.config.ActionSet;
import com.funnelback.publicui.search.model.curator.config.CuratorConfig;
import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.SearchTransactionUtils;

/**
 * The curator output processor performs curator actions which should occur
 * after the padre query has been executed (such as adding things to the data
 * model).
 * 
 * The actions to run are based on the content of the curator.yaml file within
 * the profile being queried and the matching (or not) of the triggers defined
 * for each set of actions.
 * 
 * The curator actions can be disabled as a whole by setting curator=false as a
 * URL parameter.
 */
@Component("curatorOutputProcessor")
public class Curator extends AbstractOutputProcessor {

    /**
     * Find all curator actions (in the current profile's CuratorConfig) which
     * are triggered on output for this searchTranscation, and perform them in
     * turn.
     */
    @Override
    public void processOutput(SearchTransaction searchTransaction) throws OutputProcessorException {
        CuratorModel model = new CuratorModel();
        searchTransaction.getResponse().setCuratorModel(model);

        if (SearchTransactionUtils.hasQueryAndCollection(searchTransaction)) {

            if (CuratorConfig.isCuratorActive(searchTransaction)) {

                String profileName = searchTransaction.getQuestion().getProfile();

                if (searchTransaction.getQuestion().getCollection().getProfiles().containsKey(profileName)) {
                    CuratorConfig config = searchTransaction.getQuestion().getCollection().getProfiles()
                        .get(profileName).getCuratorConfig();

                    for (Entry<Trigger, ActionSet> e : config.getTriggerActions().entrySet()) {
                        if (e.getValue().hasActionForPhase(Phase.OUTPUT) && e.getKey().activatesOn(searchTransaction)) {
                            e.getValue().performActions(searchTransaction, Phase.OUTPUT);
                        }
                    }
                }
            }
        }

    }
}
