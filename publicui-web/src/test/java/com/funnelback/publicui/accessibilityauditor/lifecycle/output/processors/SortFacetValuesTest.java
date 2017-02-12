package com.funnelback.publicui.accessibilityauditor.lifecycle.output.processors;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.funnelback.common.filter.accessibility.Metadata;
import com.funnelback.publicui.accessibilityauditor.lifecycle.output.processors.SortFacetValues.AffectedByComparator;
import com.funnelback.publicui.accessibilityauditor.lifecycle.output.processors.SortFacetValues.LevelNameComparator;
import com.funnelback.publicui.accessibilityauditor.lifecycle.output.processors.SortFacetValues.PrincipleIdComparator;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.Facet.Category;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;

public class SortFacetValuesTest {

    @Test
    public void testLevelNameComparator() {
        LevelNameComparator c = new LevelNameComparator();
        Assert.assertEquals(-1, c.compare(mockCV("A"), mockCV("AA")));
        Assert.assertEquals(1, c.compare(mockCV("AAA"), mockCV("AA")));
        Assert.assertEquals(0, c.compare(mockCV("AAA"), mockCV("AAA")));
        
        // It's just using natural order on the strings
        Assert.assertEquals(-40, c.compare(mockCV("AAA"), mockCV("invalid")));
    }
    
    @Test
    public void testPrincipleIdComparator() {
        PrincipleIdComparator c = new PrincipleIdComparator();
        Assert.assertEquals(-1, c.compare(mockCV("1"), mockCV("2")));
        Assert.assertEquals(2, c.compare(mockCV("3"), mockCV("1")));
        Assert.assertEquals(0, c.compare(mockCV("4"), mockCV("4")));
        
        Assert.assertEquals("Shouldn't throw an exception", 0, c.compare(mockCV("4"), mockCV("invalid")));
    }

    @Test
    public void testAffectedByComparator() {
        AffectedByComparator c = new AffectedByComparator();
        Assert.assertEquals(-1, c.compare(mockCV("Failure"), mockCV("Alert")));
        Assert.assertEquals(1, c.compare(mockCV("None"), mockCV("Alert")));
        Assert.assertEquals(0, c.compare(mockCV("Alert"), mockCV("Alert")));
        
        Assert.assertEquals("Shouldn't throw an exception", 0, c.compare(mockCV("Alert"), mockCV("invalid")));
    }

    @Test
    public void testPopulatePrinciples() throws OutputProcessorException {
        SearchQuestion sq = new SearchQuestion();
        sq.setQuestionType(SearchQuestionType.ACCESSIBILITY_AUDITOR);
        
        SearchResponse sr = new SearchResponse();
        SearchTransaction transaction = new SearchTransaction(sq, sr);

        // -- Levels
        Category c = new Category(null, null);
        c.getValues().add(new CategoryValue("AA", "AA", 123, null, null, false));
        c.getValues().add(new CategoryValue("A", "A", 100, null, null, false));
        c.getValues().add(new CategoryValue("AAA", "AAA", 80, null, null, false));        
        Facet f = new Facet(Metadata.getMetadataClass(Metadata.Names.failedLevels().getName()));
        f.getCategories().add(c);
        sr.getFacets().add(f);

        // -- Principles
        c = new Category(null, null);
        c.getValues().add(new CategoryValue("3", "3", 123, null, null, false));
        c.getValues().add(new CategoryValue("1", "1", 100, null, null, false));
        c.getValues().add(new CategoryValue("4", "4", 80, null, null, false));        
        c.getValues().add(new CategoryValue("2", "2", 80, null, null, false));
        f = new Facet(Metadata.getMetadataClass(Metadata.Names.principle().getName()));
        f.getCategories().add(c);
        sr.getFacets().add(f);

        // -- Affected by
        c = new Category(null, null);
        c.getValues().add(new CategoryValue("Alert", "Alert", 123, null, null, false));
        c.getValues().add(new CategoryValue("None", "None", 100, null, null, false));
        c.getValues().add(new CategoryValue("Failure", "Failure", 80, null, null, false));        
        f = new Facet(Metadata.getMetadataClass(Metadata.Names.affectedBy().getName()));
        f.getCategories().add(c);
        sr.getFacets().add(f);

        // -- Other facet
        c = new Category(null, null);
        c.getValues().add(new CategoryValue("v2", "v2", 123, null, null, false));
        c.getValues().add(new CategoryValue("v3", "v3", 100, null, null, false));
        c.getValues().add(new CategoryValue("v1", "v1", 80, null, null, false));        
        f = new Facet("OtherFacet");
        f.getCategories().add(c);
        sr.getFacets().add(f);

        new SortFacetValues().processOutput(transaction);
        
        
        Assert.assertEquals("A", sr.getFacets().get(0).getCategories().get(0).getValues().get(0).getData());
        Assert.assertEquals("AA", sr.getFacets().get(0).getCategories().get(0).getValues().get(1).getData());
        Assert.assertEquals("AAA", sr.getFacets().get(0).getCategories().get(0).getValues().get(2).getData());

        Assert.assertEquals("1", sr.getFacets().get(1).getCategories().get(0).getValues().get(0).getData());
        Assert.assertEquals("2", sr.getFacets().get(1).getCategories().get(0).getValues().get(1).getData());
        Assert.assertEquals("3", sr.getFacets().get(1).getCategories().get(0).getValues().get(2).getData());
        Assert.assertEquals("4", sr.getFacets().get(1).getCategories().get(0).getValues().get(3).getData());

        Assert.assertEquals("Failure", sr.getFacets().get(2).getCategories().get(0).getValues().get(0).getData());
        Assert.assertEquals("Alert", sr.getFacets().get(2).getCategories().get(0).getValues().get(1).getData());
        Assert.assertEquals("None", sr.getFacets().get(2).getCategories().get(0).getValues().get(2).getData());

        // Other facet non sorted
        Assert.assertEquals("v2", sr.getFacets().get(3).getCategories().get(0).getValues().get(0).getData());
        Assert.assertEquals("v3", sr.getFacets().get(3).getCategories().get(0).getValues().get(1).getData());
        Assert.assertEquals("v1", sr.getFacets().get(3).getCategories().get(0).getValues().get(2).getData());

    }

    
    private static CategoryValue mockCV(String data) {
        CategoryValue cv = Mockito.mock(CategoryValue.class);
        Mockito.when(cv.getData()).thenReturn(data);
        
        return cv;
    }
    
}

