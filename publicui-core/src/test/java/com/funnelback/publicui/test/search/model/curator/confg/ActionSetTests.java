package com.funnelback.publicui.test.search.model.curator.confg;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.funnelback.publicui.search.model.curator.config.Action;
import com.funnelback.publicui.search.model.curator.config.Action.Phase;
import com.funnelback.publicui.search.model.curator.config.ActionSet;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class ActionSetTests {
    @Test
    public void testHasActionForPhase() {
        ActionSet set = new ActionSet();
        set.getActions().add(new InputAction());
        
        Assert.assertTrue("Expected to find input action", set.hasActionForPhase(Phase.INPUT, null));
        Assert.assertFalse("Expected not to find output action", set.hasActionForPhase(Phase.OUTPUT, null));
        
        set.getActions().add(new OutputAction());

        Assert.assertTrue("Expected to find input action", set.hasActionForPhase(Phase.INPUT, null));
        Assert.assertTrue("Expected to find output action", set.hasActionForPhase(Phase.OUTPUT, null));
    }

    @Test
    public void testDualHasActionForPhase() {
        ActionSet set = new ActionSet();
        set.getActions().add(new DualAction());
        
        Assert.assertTrue("Expected to find input action", set.hasActionForPhase(Phase.INPUT, null));
        Assert.assertTrue("Expected to find output action", set.hasActionForPhase(Phase.OUTPUT, null));
    }
    
    @Test
    public void testPerformActions() {
        ActionSet set = new ActionSet();
        InputAction ia = new InputAction();
        OutputAction oa = new OutputAction();
        set.getActions().add(ia);
        set.getActions().add(oa);

        set.performActions(null, Phase.INPUT, null);

        Assert.assertTrue("Expected input action to be run", ia.hasBeenRun);
        Assert.assertFalse("Expected output action not to be run", oa.hasBeenRun);
    }

    @Test
    public void testPerformDualActions() {
        ActionSet set = new ActionSet();
        DualAction da = new DualAction();
        set.getActions().add(da);

        set.performActions(null, Phase.INPUT, null);

        Assert.assertTrue("Expected input action to be run", da.hasBeenRun);
        
        da.hasBeenRun = false;
        
        set.performActions(null, Phase.OUTPUT, null);
        
        Assert.assertTrue("Expected input action to be run", da.hasBeenRun);
    }

    private class InputAction implements Action {
        
        public boolean hasBeenRun = false;
        
        @Override
        public void performAction(SearchTransaction searchTransaction, Phase phase, ApplicationContext context) {
            hasBeenRun = true;
        }

        @Override
        public boolean runsInPhase(Phase phase, ApplicationContext context) {
            return Phase.INPUT.equals(phase);
        }
        
    }

    private class OutputAction implements Action {

        public boolean hasBeenRun = false;
        
        @Override
        public void performAction(SearchTransaction searchTransaction, Phase phase, ApplicationContext context) {
            hasBeenRun = true;
        }

        @Override
        public boolean runsInPhase(Phase phase, ApplicationContext context) {
            return Phase.OUTPUT.equals(phase);
        }
        
    }

    private class DualAction implements Action {

        public boolean hasBeenRun = false;
        
        @Override
        public void performAction(SearchTransaction searchTransaction, Phase phase, ApplicationContext context) {
            hasBeenRun = true;
        }

        @Override
        public boolean runsInPhase(Phase phase, ApplicationContext context) {
            return Phase.INPUT.equals(phase) || Phase.OUTPUT.equals(phase);
        }
        
    }
}
