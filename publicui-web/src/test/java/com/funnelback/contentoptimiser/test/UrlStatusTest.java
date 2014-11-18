package com.funnelback.contentoptimiser.test;

import org.junit.Assert;

import org.junit.Test;

import com.funnelback.contentoptimiser.UrlStatus;

public class UrlStatusTest {
    @Test
    public void testUrlStatus() {
        UrlStatus u = new UrlStatus();
        Assert.assertFalse("Blank UrlStatus shouldn't be available",u.isAvailable());
        u.setAvailable("false");
        Assert.assertFalse("False UrlStatus shouldn't be available",u.isAvailable());
        u.setAvailable("true");
        Assert.assertTrue("True UrlStatus should be available",u.isAvailable());
    }
}
