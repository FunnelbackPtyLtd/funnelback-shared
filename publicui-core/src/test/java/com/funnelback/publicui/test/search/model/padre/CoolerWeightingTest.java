package com.funnelback.publicui.test.search.model.padre;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.padre.CoolerWeighting;

public class CoolerWeightingTest {

	@Test
	public void testEquals() {
		CoolerWeighting cw1 = new CoolerWeighting("name", 0);
		CoolerWeighting cw2 = new CoolerWeighting("name", 0);
		
		Assert.assertEquals(cw1, cw2);
		Assert.assertEquals(cw1.hashCode(), cw2.hashCode());
	}
	
	@Test
	public void testDifferentNames() {
		CoolerWeighting cw1 = new CoolerWeighting("name1", 0);
		CoolerWeighting cw2 = new CoolerWeighting("name2", 0);
		
		Assert.assertNotSame(cw1, cw2);
		Assert.assertNotSame(cw1.hashCode(), cw2.hashCode());
	}

	@Test
	public void testDifferentIds() {
		CoolerWeighting cw1 = new CoolerWeighting("name", 1);
		CoolerWeighting cw2 = new CoolerWeighting("name", 2);
		
		Assert.assertNotSame(cw1, cw2);
		Assert.assertNotSame(cw1.hashCode(), cw2.hashCode());
	}
	
	@Test
	public void testNullNamesEquals() {
		CoolerWeighting cw1 = new CoolerWeighting(null, 42);
		CoolerWeighting cw2 = new CoolerWeighting(null, 42);
		
		Assert.assertEquals(cw1, cw2);
		Assert.assertEquals(cw1.hashCode(), cw2.hashCode());
	}

	@Test
	public void testNullNamesDifferent() {
		CoolerWeighting cw1 = new CoolerWeighting(null, 1);
		CoolerWeighting cw2 = new CoolerWeighting("name", 2);
		
		Assert.assertNotSame(cw1, cw2);
		Assert.assertNotSame(cw1.hashCode(), cw2.hashCode());
	}
	
	@Test
	public void testEqualsNull() {
		Assert.assertFalse(new CoolerWeighting("name", 0).equals(null));
	}

}
