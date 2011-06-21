package com.funnelback.contentoptimiser.test;

import junit.framework.Assert;

import org.junit.Test;

import com.funnelback.contentoptimiser.RankerOptions;

public class RankerOptionsTest {
	
	@Test
	public void testMetaDataWeights() {
		String optionsString = "SMqb -daat -sco2 -wmeta k 0.628 -wmeta K 0.800 -wmeta t 0.288 -k1=2.800 -b=0.420 -cool0 79.2 -cool1 3.7 -cool2 7.5 -cool3 24 -cool4 22.5 -cool5 11.2 -cool12 14.6 -cool18 80 -cool21 90 -cool22 6.5 -cool24 28 -cool26 6 -cool27 3.6 -title_dup_factor=0.100 -synonyms_enabled=0";
		RankerOptions o = new RankerOptions(optionsString);
		
		Assert.assertEquals(0.628,o.getMetaWeight("k"),0.0001);
		Assert.assertEquals(0.800,o.getMetaWeight("K"),0.0001);
		Assert.assertEquals(0.288,o.getMetaWeight("t"),0.0001);
		Assert.assertEquals(1,o.getMetaWeight("x"),0.0001); // should return 1 for unknown options
	}
}
