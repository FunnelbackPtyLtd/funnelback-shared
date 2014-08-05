package com.funnelback.publicui.curator.trigger;

import java.util.Map;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;

import com.funnelback.dataapi.connector.predictivesegmentation.PredictiveSegmentationConnector;
import com.funnelback.dataapi.connector.predictivesegmentation.PredictiveSegmentationException;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;


/**
 * <p>
 * A trigger which activates only when the current request originates user
 * matching some particular Predictive Segmenter constraint.
 * </p>
 * 
 * <p>
 * This has the actual implementation of the activatesOn method (which requires
 * a dependency on predictive segmenter (which publicui-web has and
 * publicui-core should not have).
 * </p>
 */
public class UserSegmentTrigger extends com.funnelback.publicui.search.model.curator.trigger.UserSegmentTrigger {

    /** Connector to the visitor profile data */
    @Autowired
    @Setter
    public PredictiveSegmentationConnector predictiveSegmentationConnector;

    /**
     * Check whether the given searchTransaction originates from a country
     * listed in targetCountries. If it does, return true, otherwise false.
     */
    @Override
    public boolean activatesOn(SearchTransaction searchTransaction) {
        SearchQuestion question = searchTransaction.getQuestion();
        String remoteIpAddress = question.getRequestId();
        
        Map<String, String> segmentInfo;
        try {
            segmentInfo = predictiveSegmentationConnector.getProfile(remoteIpAddress);
        } catch (PredictiveSegmentationException e) {
            throw new RuntimeException(e);
        }
        
        if (segmentInfo.containsKey(getSegmentType())) {
            if (segmentInfo.get(getSegmentType()).contains(getSegmentValue())) {
                return true;
            }
        }

        return false;
    }

}
