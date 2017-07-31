package com.funnelback.publicui.search.lifecycle.output.processors.facetednavigation;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.Category;
public class CategoryAndSiblingsTest {

    @Test
    public void testToCategoriesWithSiblings() {
        
        Facet.Category cat1 = mock(Facet.Category.class);
        Facet.Category cat2 = mock(Facet.Category.class);
        Facet.Category cat3 = mock(Facet.Category.class);
        List<CategoryAndSiblings> catAndSibs = CategoryAndSiblings.toCategoriesWithSiblings(asList(cat1, cat2, cat3));
        
        List<Category> cats = new ArrayList<>();
        for(CategoryAndSiblings cs : catAndSibs) {
            Assert.assertFalse("The list of siblings of the category,"
                + " should not contain the category", cs.getSiblings().contains(cs.getCategory()));
            
            Assert.assertFalse("We saw a category twice!", cats.contains(cs.getCategory()));
            cats.add(cs.getCategory());
            
            List<Category> siblingsWithCat = new ArrayList<>();
            siblingsWithCat.add(cs.getCategory());
            siblingsWithCat.addAll(cs.getSiblings());
            
            Assert.assertTrue("missing cat1", siblingsWithCat.contains(cat1));
            Assert.assertTrue("missing cat2", siblingsWithCat.contains(cat2));
            Assert.assertTrue("missing cat3", siblingsWithCat.contains(cat3));
            
            Assert.assertEquals("Should have 2 siblings of the category, "
                + "as we have 3 categories in total.",2, cs.getSiblings().size());
        }
        
        Assert.assertEquals("Should have seen in total 3 categories", 3, cats.size());
    }
}
