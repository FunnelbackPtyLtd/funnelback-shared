package com.funnelback.publicui.test.utils.web;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.utils.web.LocalHostnameHolder;

public class LocalHostnameHolderTest {

	@Test
	public void testAutodetect() {
		LocalHostnameHolder holder = new LocalHostnameHolder();
		
		Assert.assertNotNull(holder.getHostname());
		Assert.assertNotNull(holder.getShortHostname());
		Assert.assertTrue(holder.getHostname().startsWith(holder.getShortHostname()));
		Assert.assertFalse(holder.getShortHostname().contains("."));
		Assert.assertFalse(holder.isLocalhost());
	}

	@Test
	public void testNoAutodetect() {
		LocalHostnameHolder holder = new LocalHostnameHolder("my.host.com");
		
		Assert.assertEquals("my.host.com", holder.getHostname());
		Assert.assertEquals("my", holder.getShortHostname());
		Assert.assertFalse(holder.isLocalhost());
	}
	
	@Test
	public void testLocalhost() {
		LocalHostnameHolder holder = new LocalHostnameHolder("localhost");
		
		Assert.assertEquals("localhost", holder.getHostname());
		Assert.assertEquals("localhost", holder.getShortHostname());
		Assert.assertTrue(holder.isLocalhost());
	}
	
}
