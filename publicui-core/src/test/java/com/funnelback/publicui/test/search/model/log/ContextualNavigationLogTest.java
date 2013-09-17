package com.funnelback.publicui.test.search.model.log;

import java.util.Arrays;
import java.util.Calendar;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.log.ContextualNavigationLog;

public class ContextualNavigationLogTest {

    @Test
    public void testToXml() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, 2013);
        c.set(Calendar.MONTH, Calendar.SEPTEMBER);
        c.set(Calendar.DAY_OF_MONTH, 15);
        c.set(Calendar.HOUR_OF_DAY, 13);
        c.set(Calendar.MINUTE, 25);
        c.set(Calendar.SECOND, 42);
        
        ContextualNavigationLog l = new ContextualNavigationLog(
            c.getTime(),
            new Collection("collection", null),
            new Profile("profile"),
            "request-id",
            "cluster",
            Arrays.asList(new String[] {"clus1", "clus2"} ),
            "user-id");
        
        Assert.assertEquals(
            "<cflus>"
            + "<t>20130915 13:25:42</t>"
            + "<cluster0>clus1</cluster0>"
            + "<cluster1>clus2</cluster1>"
            + "<coll>collection</coll>"
            + "<fluster>cluster</fluster>"
            + "<prof>profile</prof>"
            + "<requestip>request-id</requestip>"
            + "</cflus>",
            l.toXml());
    }
    
}
