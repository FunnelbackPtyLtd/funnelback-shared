package com.funnelback.publicui.accessibilityauditor.lifecycle.output.processors;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.funnelback.common.filter.accessibility.Metadata;
import com.funnelback.publicui.accessibilityauditor.lifecycle.output.processors.SortFacetValues.AffectedByComparator;
import com.funnelback.publicui.accessibilityauditor.lifecycle.output.processors.SortFacetValues.LevelNameComparator;
import com.funnelback.publicui.accessibilityauditor.lifecycle.output.processors.SortFacetValues.PrincipleIdComparator;
import com.funnelback.publicui.accessibilityauditor.lifecycle.output.processors.SortFacetValues.SuccessCriterionNameComparator;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.Category;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class SortFacetValuesTest {

    @Test
    public void testSuccessCriterionNameComparator() {
        SuccessCriterionNameComparator c = new SuccessCriterionNameComparator();
        Assert.assertTrue(c.compare(mockCVLabel("1.1.1 - Non-text Content"), mockCVLabel("1.4.3 - Contrast (Minimum)")) < 0);
        Assert.assertTrue(c.compare(mockCVLabel("3.1.1 - Language of Page"), mockCVLabel("1.4.3 - Contrast (Minimum)")) > 0);
        Assert.assertEquals(0, c.compare(mockCVLabel("1.4.3 - Contrast (Minimum)"), mockCVLabel("1.4.3 - Contrast (Minimum)")));
    }

    @Test
    public void testLevelNameComparator() {
        LevelNameComparator c = new LevelNameComparator();
        Assert.assertEquals(-1, c.compare(mockCVData("A"), mockCVData("AA")));
        Assert.assertEquals(1, c.compare(mockCVData("AAA"), mockCVData("AA")));
        Assert.assertEquals(0, c.compare(mockCVData("AAA"), mockCVData("AAA")));

        // It's just using natural order on the strings
        Assert.assertEquals(-40, c.compare(mockCVData("AAA"), mockCVData("invalid")));
    }

    @Test
    public void testPrincipleIdComparator() {
        PrincipleIdComparator c = new PrincipleIdComparator();
        Assert.assertEquals(-1, c.compare(mockCVData("1"), mockCVData("2")));
        Assert.assertEquals(2, c.compare(mockCVData("3"), mockCVData("1")));
        Assert.assertEquals(0, c.compare(mockCVData("4"), mockCVData("4")));

        Assert.assertEquals("Shouldn't throw an exception", 0, c.compare(mockCVData("4"), mockCVData("invalid")));
    }

    @Test
    public void testAffectedByComparator() {
        AffectedByComparator c = new AffectedByComparator();
        Assert.assertEquals(-1, c.compare(mockCVData("Failure"), mockCVData("Alert")));
        Assert.assertEquals(1, c.compare(mockCVData("None"), mockCVData("Alert")));
        Assert.assertEquals(0, c.compare(mockCVData("Alert"), mockCVData("Alert")));

        Assert.assertEquals("Shouldn't throw an exception", 0, c.compare(mockCVData("Alert"), mockCVData("invalid")));
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

    private static CategoryValue mockCVLabel(String label) {
        CategoryValue cv = Mockito.mock(CategoryValue.class);
        Mockito.when(cv.getLabel()).thenReturn(label);
        return cv;
    }

    private static CategoryValue mockCVData(String data) {
        CategoryValue cv = Mockito.mock(CategoryValue.class);
        Mockito.when(cv.getData()).thenReturn(data);

        return cv;
    }

}
