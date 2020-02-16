package com.funnelback.publicui.test.search.model.curator.confg.trigger;



import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.curator.CuratorClasses;
import com.funnelback.publicui.search.model.curator.config.Trigger;

public class TriggerEqualsTest {

    @Test
    public void testEquals() throws Exception {
        for(Class<?> clazz : CuratorClasses.getTriggerClasses()) {
            Trigger t1 = (Trigger) clazz.getConstructor().newInstance();
            Trigger t2 = (Trigger) clazz.getConstructor().newInstance();
            Assert.assertEquals(t1, t2);
        }
    }
}
