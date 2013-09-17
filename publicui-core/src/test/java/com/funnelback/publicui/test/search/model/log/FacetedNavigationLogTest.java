package com.funnelback.publicui.test.search.model.log;

import java.util.Calendar;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.collection.Profile;
import com.funnelback.publicui.search.model.log.FacetedNavigationLog;

public class FacetedNavigationLogTest {

    @Test
    public void testToXml() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, 2013);
        c.set(Calendar.MONTH, Calendar.SEPTEMBER);
        c.set(Calendar.DAY_OF_MONTH, 15);
        c.set(Calendar.HOUR_OF_DAY, 13);
        c.set(Calendar.MINUTE, 25);
        c.set(Calendar.SECOND, 42);
        
        FacetedNavigationLog l = new FacetedNavigationLog(
            c.getTime(),
            new Collection("collection", null),
            new Profile("profile"),
            "request-id",
            "user-id",
            "facet",
            "query");
        
        Assert.assertEquals(
            "<cfac>"
            + "<t>20130915 13:25:42</t>"
            + "<coll>collection</coll>"
            + "<facet>facet</facet>"
            + "<prof>profile</prof>"
            + "<requestip>request-id</requestip>"
            + "<squery>query</squery>"
            + "</cfac>",
            l.toXml());
    }
    
}
