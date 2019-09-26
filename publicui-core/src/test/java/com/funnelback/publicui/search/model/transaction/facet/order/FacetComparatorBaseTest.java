package com.funnelback.publicui.search.model.transaction.facet.order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import org.apache.tools.ant.taskdefs.optional.junit.BaseTest;

import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;

@RunWith(Parameterized.class)
public abstract class FacetComparatorBaseTest extends BaseTest {
    
    @Parameter(0)
    public List<CategoryValue> input;
    @Parameter(1)
    public List<CategoryValue> sorted;
    
    public abstract Comparator<CategoryValue> getComparator();
    
    public abstract CategoryValue getNonNullValue();
    
    public abstract Optional<CategoryValue> getNullValue();
    
    public abstract boolean getNullsLast();
    
    @Test
    public void checkSorting() {
        List<CategoryValue> toSort = new ArrayList<>(input);
        Collections.sort(toSort, getComparator());
        Assert.assertEquals(sorted, toSort);
    }
    
    @Test
    public void testNulls() {
        Assume.assumeTrue(getNullValue().isPresent());
        Assert.assertEquals(0, getComparator().compare(getNullValue().get(), getNullValue().get()));
    }
    
    @Test
    public void testNullsOrder() {
        Assume.assumeTrue(getNullValue().isPresent());
        int o1 = getComparator().compare(getNullValue().get(), getNonNullValue());
        int o2 = getComparator().compare(getNonNullValue(), getNullValue().get());
        
        if(getNullsLast()) {
            Assert.assertTrue(o1 > 0);
            Assert.assertTrue(o2 < 0);
        } else {
            Assert.assertTrue(o1 < 0);
            Assert.assertTrue(o2 > 0);
        }
    }
    
    @Test
    public void testNullVsNonNull() {
        Assume.assumeTrue(getNullValue().isPresent());
        int o1 = getComparator().compare(getNullValue().get(), getNonNullValue());
        int o2 = getComparator().compare(getNonNullValue(), getNullValue().get());
        Assert.assertNotEquals(0, o1);
        Assert.assertNotEquals(0, o2);
        Assert.assertEquals(o1, o2*-1);
        Assert.assertNotEquals(o1, o2);
    }
    
    @Test
    public void checkEquals() {
        for(int i = 0; i < input.size(); i++) {
            CategoryValue cv = input.get(i);
            Assert.assertEquals(0, getComparator().compare(cv, cv));
        }
    }
    
    @Test
    public void checkReversable() {
        for(int i = 0; i < input.size(); i++) {
            CategoryValue cv1 = input.get(i);
            for(int j = 0; j < input.size(); j++) {
                CategoryValue cv2 = input.get(j);
                int order1 = getComparator().compare(cv1, cv2);
                int order2 = getComparator().compare(cv2, cv1);
                
                Assert.assertEquals("Something is wrong in the way comparison is done! This test took:\n"
                    + "CategoryValues " + 
                    i + ": " + cv1 + "\n" + 
                    j + ": " + cv2 + "\n" +
                    "Comparing one way gave: " + order1 + " comparing the other way gave " + order2 + " multipling by -1 should give the other!",
                    order1, order2 * -1);
            }
        }
    }
    
    
    @Test
    public void checkReversableAgainstNulls() {
        Assume.assumeTrue(getNullValue().isPresent());
        for(int i = 0; i < input.size(); i++) {
            CategoryValue cv1 = input.get(i);
            CategoryValue cv2 = getNullValue().get();
            int order1 = getComparator().compare(cv1, cv2);
            int order2 = getComparator().compare(cv2, cv1);
            
            Assert.assertEquals("Something is wrong in the way comparison is done! This test took:\n"
                + "CategoryValues " + 
                i + ": " + cv1 + "\n" + 
                " vs a null value\n" + 
                "Comparing one way gave: " + order1 + " comparing the other way gave " + order2 + " multipling by -1 should give the other!",
                order1, order2 * -1);
            
        }
    }

}
