package com.funnelback.publicui.test.search.service.log;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.LocaleResolver;

import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.common.config.DefaultValues.RequestIdToLog;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.log.Log;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.service.log.LogUtils;
import com.funnelback.publicui.search.web.binding.SearchQuestionBinder;

public class LogUtilsTests {
    
    @Test
    public void testGetRequestIdentifier() {
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
    
    @Test 
    public void testGetIpRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        
        Assert.assertEquals("1.2.3.4", LogUtils.getRequestIdentifier(request, RequestIdToLog.ip));
    }
    
    @Test
    public void testGetIpForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        request.addHeader("X-Forwarded-For", "5.6.7.8");
        
        Assert.assertEquals("5.6.7.8", LogUtils.getRequestIdentifier(request, RequestIdToLog.ip));
    }
    
    @Test
    public void testGetIpForwardedForMultiple() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        request.addHeader("X-Forwarded-For", "5.6.7.8, 9.10.11.12, 13.14.15.16");
        
        Assert.assertEquals("5.6.7.8", LogUtils.getRequestIdentifier(request, RequestIdToLog.ip));
    }

    @Test
    public void testGetIpForwardedForMultipleSpaces() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        request.addHeader("X-Forwarded-For", "   5.6.7.8  ,   9.10.11.12    ,   13.14.15.16   ");
        
        Assert.assertEquals("5.6.7.8", LogUtils.getRequestIdentifier(request, RequestIdToLog.ip));
    }
    
    @Test
    public void testGetIpForwardedForPrivate() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        request.addHeader("X-Forwarded-For", "10.1.2.3");
        
        Assert.assertEquals("1.2.3.4", LogUtils.getRequestIdentifier(request, RequestIdToLog.ip));
    }
    
    @Test
    public void testGetIpForwardedForPrivateAndPublic() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        request.addHeader("X-Forwarded-For", "10.1.2.3, 5.6.7.8");
        
        Assert.assertEquals("5.6.7.8", LogUtils.getRequestIdentifier(request, RequestIdToLog.ip));
    }
    

}
