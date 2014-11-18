package com.funnelback.contentoptimiser.test;

import org.junit.Assert;

import org.junit.Ignore;
import org.junit.Test;

import com.funnelback.contentoptimiser.processors.impl.RankerOptions;

public class RankerOptionsTest {
    
    @Test
    public void testMetaDataWeights() {
        String optionsString = "SMqb -daat -sco2 -wmeta k 0.628 -wmeta K 0.800 -wmeta t 0.288 -k1=2.800 -b=0.420 -cool0 79.2 -cool1 3.7 -cool2 7.5 -cool3 24 -cool4 22.5 -cool5 11.2 -cool12 14.6 -cool18 80 -cool21 90 -cool22 6.5 -cool24 28 -cool26 6 -cool27 3.6 -title_dup_factor=0.100 -synonyms_enabled=0";
        RankerOptions o = new RankerOptions();
        o.consume(optionsString);
        
        Assert.assertEquals(0.628,o.getMetaWeight("k"),0.0001);
        Assert.assertEquals(0.800,o.getMetaWeight("K"),0.0001);
        Assert.assertEquals(0.288,o.getMetaWeight("t"),0.0001);
        Assert.assertEquals(0,o.getMetaWeight("x"),0.0001); // should return 0 for unknown options
    }

    @Test
    public void testDefaults() {
        String optionsString = "SMqb -daat -sco2";
        RankerOptions o = new RankerOptions();
        o.consume(optionsString);
        Assert.assertEquals("default should be 0.5",0.5,o.getMetaWeight("k"),0.0001);
        Assert.assertEquals("default should be 0.5",0.5,o.getMetaWeight("K"),0.0001);
        Assert.assertEquals("default should be 1.0", 1.0,o.getMetaWeight("t"),0.0001);
        Assert.assertEquals("Should return 0 for unknown options",0,o.getMetaWeight("x"),0.0001); // should return 0 for unknown options
        
        optionsString = "SMqb -daat -sco7";
        
        o = new RankerOptions();
        o.consume(optionsString);
        
        Assert.assertEquals("default should be 0.5",0.5,o.getMetaWeight("k"),0.0001);
        Assert.assertEquals("default should be 0.5",0.5,o.getMetaWeight("K"),0.0001);
        Assert.assertEquals("default should be 1.0", 1.0,o.getMetaWeight("t"),0.0001);
        Assert.assertEquals("Should return 0 for unknown options",0,o.getMetaWeight("x"),0.0001); // should return 0 for unknown options
    
    }
    
    @Test
    public void testExplicit() {
        
        String optionsString = "SMqb -daat -sco7t";
        RankerOptions o = new RankerOptions();
        o.consume(optionsString);
        Assert.assertEquals("should be set to 0 if not explicitly enabled",0.0,o.getMetaWeight("k"),0.0001);
        Assert.assertEquals("should be set to 0 if not explicitly enabled",0.0,o.getMetaWeight("K"),0.0001);
        Assert.assertEquals("default should be 1.0", 1.0,o.getMetaWeight("t"),0.0001);
        Assert.assertEquals("Should return 0 for unknown options",0,o.getMetaWeight("x"),0.0001); // should return 0 for unknown options

        optionsString = "SMqb -daat -sco7tK";
        o = new RankerOptions();
        o.consume(optionsString);
        Assert.assertEquals("should be set to 0 if not explicitly enabled",0.0,o.getMetaWeight("k"),0.0001);
        Assert.assertEquals("default should be 0.5",0.5,o.getMetaWeight("K"),0.0001);
        Assert.assertEquals("default should be 1.0", 1.0,o.getMetaWeight("t"),0.0001);
        Assert.assertEquals("Should return 0 for unknown options",0,o.getMetaWeight("x"),0.0001); // should return 0 for unknown options

        optionsString = "SMqb -daat -sco7t -wmeta k 0.6";
        o = new RankerOptions();
        o.consume(optionsString);
        Assert.assertEquals("explicitly set to 0.6",0.6,o.getMetaWeight("k"),0.0001);
        Assert.assertEquals("should be set to 0 if not explicitly enabled",0.0,o.getMetaWeight("K"),0.0001);
        Assert.assertEquals("default should be 1.0", 1.0,o.getMetaWeight("t"),0.0001);
        Assert.assertEquals("Should return 0 for unknown options",0,o.getMetaWeight("x"),0.0001); // should return 0 for unknown options

    }
    
    @Test @Ignore
    public void testUVWeights() {
        // ignored this test, since -uv only exists in documentation and not actual padre!
        String optionsString = "SMqb -daat -sco2 -uv";
        RankerOptions o = new RankerOptions();
        o.consume(optionsString);
        
        Assert.assertEquals(1.0,o.getMetaWeight("u"),0.0001);
        Assert.assertEquals(1.0,o.getMetaWeight("v"),0.0001);
    }

    
}
