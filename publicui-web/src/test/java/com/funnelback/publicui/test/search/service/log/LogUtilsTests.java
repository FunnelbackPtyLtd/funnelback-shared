package com.funnelback.publicui.test.search.service.log;

import org.junit.Assert;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.funnelback.common.config.DefaultValues.RequestId;
import com.funnelback.publicui.search.model.log.Log;
import com.funnelback.publicui.search.service.log.LogUtils;

public class LogUtilsTests {
    
    @Test
    public void testGetRequestIdentifier() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        Assert.assertEquals("1.2.3.4", LogUtils.getRequestIdentifier(request, RequestId.ip, null));
        Assert.assertEquals(DigestUtils.md5Hex("1.2.3.4"), LogUtils.getRequestIdentifier(request, RequestId.ip_hash, null));
        Assert.assertEquals(Log.REQUEST_ID_NOTHING, LogUtils.getRequestIdentifier(request, RequestId.nothing, null));
        try {
            LogUtils.getRequestIdentifier(request, null, null);
            Assert.fail();
        } catch (NullPointerException npe) {}
        
        Assert.assertEquals(Log.REQUEST_ID_NOTHING, LogUtils.getRequestIdentifier(null, RequestId.ip, null));
        
        // Invalid host
        request.setRemoteAddr("\n");
        Assert.assertEquals(Log.REQUEST_ID_NOTHING, LogUtils.getRequestIdentifier(request, RequestId.ip, null));
    }
    
    @Test 
    public void testGetIpRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        
        Assert.assertEquals("1.2.3.4", LogUtils.getRequestIdentifier(request, RequestId.ip, null));
    }
    
    @Test
    public void testGetIpForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        request.addHeader("X-Forwarded-For", "5.6.7.8");
        
        Assert.assertEquals("5.6.7.8", LogUtils.getRequestIdentifier(request, RequestId.ip, null));
    }
    
    @Test
    public void testGetIpForwardedForMultiple() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        request.addHeader("X-Forwarded-For", "5.6.7.8, 9.10.11.12, 13.14.15.16");
        
        Assert.assertEquals("13.14.15.16", LogUtils.getRequestIdentifier(request, RequestId.ip, null));
    }

    @Test
    public void testGetIpForwardedForMultipleSpaces() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        request.addHeader("X-Forwarded-For", "   5.6.7.8  ,   9.10.11.12    ,   13.14.15.16   ");
        
        Assert.assertEquals("13.14.15.16", LogUtils.getRequestIdentifier(request, RequestId.ip, null));
    }
    
    @Test
    public void testGetIpForwardedForMultipleWithIgnoredRange() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        request.addHeader("X-Forwarded-For", "5.6.7.8, 9.10.11.12, 13.14.15.16");
        
        Assert.assertEquals("9.10.11.12", LogUtils.getRequestIdentifier(request, RequestId.ip, "13.14.15.16/32"));
    }

}
