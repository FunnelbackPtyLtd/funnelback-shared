package com.funnelback.publicui.search.model.transaction.session;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.padre.Result;
import com.google.common.collect.Sets;

public class ClickHistoryTest {

    @Test
    public void fromResultTest() {
        Result r = new Result();
        r.setIndexUrl("http://example.com/");
        r.setTitle("");
        r.setSummary("");
        r.getMetaData().put("a", "av");
        r.getMetaData().put("b", "bv");
        ClickHistory ch = ClickHistory.fromResult(r, Sets.newHashSet("a","c"));
        Assert.assertTrue(ch.getMetaData().containsKey("a"));
        Assert.assertFalse(ch.getMetaData().containsKey("b"));
    }
    
}
