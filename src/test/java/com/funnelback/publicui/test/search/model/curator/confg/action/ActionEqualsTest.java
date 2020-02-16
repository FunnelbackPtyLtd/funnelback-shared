package com.funnelback.publicui.test.search.model.curator.confg.action;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.curator.CuratorClasses;
import com.funnelback.publicui.search.model.curator.config.Action;

public class ActionEqualsTest {

    @Test
    public void testEquals() throws Exception {
        for(Class<?> clazz : CuratorClasses.getActionClasses()) {
            Action a1 = (Action) clazz.getConstructor().newInstance();
            Action a2 = (Action) clazz.getConstructor().newInstance();
            Assert.assertEquals(a1, a2);
        }
    }
}
