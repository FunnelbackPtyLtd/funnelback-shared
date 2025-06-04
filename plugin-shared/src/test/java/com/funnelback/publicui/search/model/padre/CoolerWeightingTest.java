package com.funnelback.publicui.search.model.padre;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CoolerWeightingTest {

    @Test
    public void testEquals() {
        CoolerWeighting cw1 = new CoolerWeighting("name", 0);
        CoolerWeighting cw2 = new CoolerWeighting("name", 0);
        
        Assertions.assertEquals(cw1, cw2);
        Assertions.assertEquals(cw1.hashCode(), cw2.hashCode());
    }
    
    @Test
    public void testDifferentNames() {
        CoolerWeighting cw1 = new CoolerWeighting("name1", 0);
        CoolerWeighting cw2 = new CoolerWeighting("name2", 0);
        
        Assertions.assertNotSame(cw1, cw2);
        Assertions.assertNotSame(cw1.hashCode(), cw2.hashCode());
    }

    @Test
    public void testDifferentIds() {
        CoolerWeighting cw1 = new CoolerWeighting("name", 1);
        CoolerWeighting cw2 = new CoolerWeighting("name", 2);
        
        Assertions.assertNotSame(cw1, cw2);
        Assertions.assertNotSame(cw1.hashCode(), cw2.hashCode());
    }
    
    @Test
    public void testNullNamesEquals() {
        CoolerWeighting cw1 = new CoolerWeighting(null, 42);
        CoolerWeighting cw2 = new CoolerWeighting(null, 42);
        
        Assertions.assertEquals(cw1, cw2);
        Assertions.assertEquals(cw1.hashCode(), cw2.hashCode());
    }

    @Test
    public void testNullNamesDifferent() {
        CoolerWeighting cw1 = new CoolerWeighting(null, 1);
        CoolerWeighting cw2 = new CoolerWeighting("name", 2);
        
        Assertions.assertNotSame(cw1, cw2);
        Assertions.assertNotSame(cw1.hashCode(), cw2.hashCode());
    }

    @Test
    public void testEqualsNull() {
        Assertions.assertNotEquals(null, new CoolerWeighting("name", 0));
    }

}
