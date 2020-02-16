package com.funnelback.publicui.search.model.padre.factories;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.padre.ResultsSummary;
import com.funnelback.publicui.search.model.padre.factories.ResultsSummaryFactory.ResultSummaryFiller;

public class ResultSummaryFactoryTest {

    @Test
    public void fromDataTestTranslucentFields() {
        
        // seems pointless, i guess it tests the class of ResultSummary in that it does not
        // assume it to be zero.
        ResultsSummary rs = new ResultsSummary();
        Assert.assertNull("When DLS is not set this value will be null", 
                            rs.getTotalSecurityObscuredUrls());
        
        rs = new ResultsSummary();
        new ResultSummaryFiller(rs).onTag("total_security_obscured_urls", "123");
        Assert.assertEquals(123, rs.getTotalSecurityObscuredUrls() + 0);
    }
}
