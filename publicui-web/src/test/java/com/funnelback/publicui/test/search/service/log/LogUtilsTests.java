package com.funnelback.publicui.test.search.service.log;

import junit.framework.Assert;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.funnelback.common.config.DefaultValues.UserIdToLog;
import com.funnelback.publicui.search.model.log.Log;
import com.funnelback.publicui.search.service.log.LogUtils;

public class LogUtilsTests {
	
	@Test
	public void testGetUserIdentifier() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRemoteAddr("1.2.3.4");
		Assert.assertEquals("1.2.3.4", LogUtils.getUserIdentifier(request, UserIdToLog.ip));
		Assert.assertEquals(DigestUtils.md5Hex("1.2.3.4"), LogUtils.getUserIdentifier(request, UserIdToLog.ip_hash));
		Assert.assertEquals(Log.USERID_NOTHING, LogUtils.getUserIdentifier(request, UserIdToLog.nothing));
		try {
			LogUtils.getUserIdentifier(request, null);
			Assert.fail();
		} catch (NullPointerException npe) {}
		
		Assert.assertEquals(Log.USERID_NOTHING, LogUtils.getUserIdentifier(null, UserIdToLog.ip));
		
		// Invalid host
		request.setRemoteAddr("\n");
		Assert.assertEquals(Log.USERID_NOTHING, LogUtils.getUserIdentifier(request, UserIdToLog.ip));
	}

}
