package com.funnelback.publicui.test.log;

import java.net.InetAddress;
import java.net.UnknownHostException;

import junit.framework.Assert;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import com.funnelback.common.config.DefaultValues.UserIdToLog;
import com.funnelback.publicui.search.service.log.LogUtils;

public class LogUtilsTests {
	
	@Test
	public void testGetUserIdentifier() throws UnknownHostException {
		InetAddress addr = InetAddress.getByName("1.2.3.4");
		Assert.assertEquals(addr.getHostAddress(), LogUtils.getUserIdentifier(addr, UserIdToLog.ip));
		Assert.assertEquals(DigestUtils.md5Hex(addr.getHostAddress()), LogUtils.getUserIdentifier(addr, UserIdToLog.ip_hash));
		Assert.assertEquals(LogUtils.USERID_NOTHING, LogUtils.getUserIdentifier(addr, UserIdToLog.nothing));
		try {
			LogUtils.getUserIdentifier(addr, null);
			Assert.fail();
		} catch (NullPointerException npe) {}
	}

}
