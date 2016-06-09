package com.funnelback.publicui.search.model.padre.factories;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.padre.ResultsSummary;

public class ResultSummaryFactoryTest {

    @Test
    public void fromDataTestTranslucentFields() {
        Map<String, Integer> data = new HashMap<>();
        ResultsSummary rs = ResultsSummaryFactory.fromData(data, false);
        Assert.assertNull("When DLS is not set this value will be null", 
                            rs.getTotalSecurityObscuredUrls());
        
        data.put("total_security_obscured_urls", 123);
        rs = ResultsSummaryFactory.fromData(data, false);
        Assert.assertEquals(123, rs.getTotalSecurityObscuredUrls() + 0);
        
    }
}
