package com.funnelback.publicui.test.search.service.log;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Ignore;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.common.config.DefaultValues.RequestId;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.log.CartClickLog;
import com.funnelback.publicui.search.model.log.Log;
import com.funnelback.publicui.search.model.transaction.session.SearchUser;
import com.funnelback.publicui.search.service.log.LogUtils;

public class LogUtilsTests {

    @Test
    public void testGetRequestIdentifier() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        Assert.assertEquals("1.2.3.4", LogUtils.getRequestIdentifier(request, RequestId.ip, null));
        Assert.assertEquals(DigestUtils.md5Hex("1.2.3.4"),
            LogUtils.getRequestIdentifier(request, RequestId.ip_hash, null));
        Assert.assertEquals(Log.REQUEST_ID_NOTHING, LogUtils.getRequestIdentifier(request, RequestId.nothing, null));
        try {
            LogUtils.getRequestIdentifier(request, null, null);
            Assert.fail();
        } catch (NullPointerException npe) {
        }

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
    public void testGetIpv6RemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("2001:db8:85a3:0:0:8a2e:0370:7334");
        // Note that leading zeros will be stripped by the processing
        // below - I guess that's good for canonicalisation.
        
        Assert.assertEquals("2001:db8:85a3:0:0:8a2e:370:7334", LogUtils.getRequestIdentifier(request, RequestId.ip, null));
    }

    @Test
    public void testGetIpForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        request.addHeader("X-Forwarded-For", "5.6.7.8");

        Assert.assertEquals("5.6.7.8", LogUtils.getRequestIdentifier(request, RequestId.ip, null));
    }

    @Test
    @Ignore // Fails for now (ignores the header) - We should fix it I think - See FUN-9701
    public void testGetIpv6ForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("2001:db8:85a3:0:0:8a2e:0370:7334");
        request.addHeader("X-Forwarded-For", "2004:db8:85a3:0:0:8a2e:0370:7334");

        Assert.assertEquals("2004:db8:85a3:0:0:8a2e:370:7334", LogUtils.getRequestIdentifier(request, RequestId.ip, null));
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

    @Test
    public void testCartLogCanBeCreated() throws Exception {
        // Given a log utility

        // When a cart log is created
        MockHttpServletRequest request = new MockHttpServletRequest();
        SearchUser user = new SearchUser(UUID.randomUUID().toString());
        Collection collection = new Collection("dummy", new NoOptionsConfig(new File(
            "src/test/resources/dummy-search_home"), "dummy"));

        CartClickLog cartLog = LogUtils.createCartLog(URI.create("funnelback://result/"), request, collection,
            CartClickLog.Type.ADD_TO_CART, user);

        // Then CartLog must be instantiated
        Assert.assertNotNull(cartLog);
    }

    @Test
    public void testThatRefererCanBeExtracted() {
        // Given a mock request with referer header set
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("referer", "https://example.com");

        // When referer is extracted
        URL referer = LogUtils.getReferrer(mockRequest);

        // Then referer URL should be extracted
        Assert.assertEquals("https://example.com", referer.toString());
    }

    @Test
    public void testThatInvalidRefererReturnsEmpty() {
        // Given a mock request with referer header not set
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        // When referer is extracted
        URL referer = LogUtils.getReferrer(mockRequest);

        // Then referer URL should not be present
        Assert.assertNull(referer);
    }

    @Test
    public void testThatBlankRefererReturnsEmpty() {
        // Given a mock request with invalid referer header set
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("referer", " ");

        // When referer is extracted
        URL referer = LogUtils.getReferrer(mockRequest);

        // Then referer URL should not be present
        Assert.assertNull(referer);
    }
}
