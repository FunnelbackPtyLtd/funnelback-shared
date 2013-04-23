package com.funnelback.publicui.test.search.service.log;

import junit.framework.Assert;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.funnelback.common.config.DefaultValues.RequestIdToLog;
import com.funnelback.publicui.search.model.log.Log;
import com.funnelback.publicui.search.service.log.LogUtils;

public class LogUtilsTests {
    
    @Test
    public void testGetUserIdentifier() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        Assert.assertEquals("1.2.3.4", LogUtils.getRequestIdentifier(request, RequestIdToLog.ip));
        Assert.assertEquals(DigestUtils.md5Hex("1.2.3.4"), LogUtils.getRequestIdentifier(request, RequestIdToLog.ip_hash));
        Assert.assertEquals(Log.REQUEST_ID_NOTHING, LogUtils.getRequestIdentifier(request, RequestIdToLog.nothing));
        try {
            LogUtils.getRequestIdentifier(request, null);
            Assert.fail();
        } catch (NullPointerException npe) {}
        
        Assert.assertEquals(Log.REQUEST_ID_NOTHING, LogUtils.getRequestIdentifier(null, RequestIdToLog.ip));
        
        // Invalid host
        request.setRemoteAddr("\n");
        Assert.assertEquals(Log.REQUEST_ID_NOTHING, LogUtils.getRequestIdentifier(request, RequestIdToLog.ip));
    }

}
