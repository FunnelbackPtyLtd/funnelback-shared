package com.funnelback.publicui.test.search.model.curator.confg;



import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.curator.action.DisplayMessage;
import com.funnelback.publicui.search.model.curator.config.Trigger;
import com.funnelback.publicui.search.model.curator.config.TriggerActions;
import com.funnelback.publicui.search.model.curator.data.Message;
import com.funnelback.publicui.search.model.curator.trigger.AndTrigger;
import com.funnelback.publicui.search.model.curator.trigger.OrTrigger;

public class TriggerActionsTest {

    @Test
    public void testEquals() {
        TriggerActions triggerActions1 = new TriggerActions();
        TriggerActions triggerActions2 = new TriggerActions();
        Assert.assertEquals(triggerActions1, triggerActions2);
    }
    
    @Test
    public void testEqualsMoreComplex() {
        TriggerActions triggerActions1 = new TriggerActions();
        setup(triggerActions1);
        
        TriggerActions triggerActions2 = new TriggerActions();
        setup(triggerActions2);
        
        Assert.assertEquals(triggerActions1, triggerActions2);
    }
    
    private static void setup(TriggerActions triggerActions) {
        
        Trigger t = new OrTrigger();
        List<Trigger> triggers = new LinkedList<>();
        triggers.add(t);
        AndTrigger andTrigger = new AndTrigger(triggers);
        triggerActions.setTrigger(andTrigger);
        
        DisplayMessage dm = new DisplayMessage();
        dm.setMessage(new Message("hello", new HashMap<String, Object>(), "cat"));
        
        triggerActions.getActions().add(dm);
    }
}
