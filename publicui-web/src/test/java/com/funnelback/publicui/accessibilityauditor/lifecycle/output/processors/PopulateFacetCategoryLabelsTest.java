package com.funnelback.publicui.accessibilityauditor.lifecycle.output.processors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.funnelback.common.filter.accessibility.Metadata;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.Category;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.wcag.checker.FailureType;
import com.funnelback.wcag.checker.html.DocumentTitleChecker;
import com.funnelback.wcag.checker.pdf.PDFTitleChecker;

public class PopulateFacetCategoryLabelsTest {

    private PopulateFacetCategoryLabels processor;
    
    private SearchTransaction transaction;
    
    @Before
    public void beforeEach() {
        processor = new PopulateFacetCategoryLabels();
        
        SearchQuestion sq = new SearchQuestion();
        sq.setQuestionType(SearchQuestionType.ACCESSIBILITY_AUDITOR);
        
        SearchResponse sr = new SearchResponse();
        transaction = new SearchTransaction(sq, sr);
    }
    
    @Test
    public void testNoFacets() throws OutputProcessorException {
        processor.processAccessibilityAuditorTransaction(transaction);
    }
    
    @Test
    public void testPopulatePrinciples() throws OutputProcessorException {
        Category c = new Category(null, null);
        c.getValues().add(new CategoryValue("1", "1", 0, null, null, false));
        c.getValues().add(new CategoryValue("2", "2", 0, null, null, false));
        c.getValues().add(new CategoryValue("unknown", "unknown", 0, null, null, false));
        
        Facet f = new Facet(Metadata.getMetadataClass(Metadata.Names.principle().getName()));
        f.getCategories().add(c);
        transaction.getResponse().getFacets().add(f);
        
        processor.processOutput(transaction);
        
        Category actual = transaction.getResponse().getFacets().get(0).getCategories().get(0);
        
        Assert.assertEquals("Perceivable", actual.getValues().get(0).getLabel());
        Assert.assertEquals("1", actual.getValues().get(0).getData());

        Assert.assertEquals("Operable", actual.getValues().get(1).getLabel());
        Assert.assertEquals("2", actual.getValues().get(1).getData());

        Assert.assertEquals("unknown", actual.getValues().get(2).getLabel());
        Assert.assertEquals("unknown", actual.getValues().get(2).getData());

    }

    @Test
    public void testPopulateSuccessCriteria() throws OutputProcessorException {
        for (FailureType type: FailureType.values()) {
            Category c = new Category(null, null);
            c.getValues().add(new CategoryValue("1.2.3", "1.2.3", 0, null, null, false));
            c.getValues().add(new CategoryValue("2.2.1", "2.2.1", 0, null, null, false));
            c.getValues().add(new CategoryValue("unknown", "unknown", 0, null, null, false));
            
            Facet f = new Facet(Metadata.getMetadataClass(Metadata.Names.successCriterion(type).getName()));
            f.getCategories().add(c);
            transaction.getResponse().getFacets().add(f);
            
            processor.processOutput(transaction);
            
            Category actual = transaction.getResponse().getFacets().get(0).getCategories().get(0);
            
            Assert.assertEquals("1.2.3 - Audio Description or Media Alternative (Prerecorded)", actual.getValues().get(0).getLabel());
            Assert.assertEquals("1.2.3", actual.getValues().get(0).getData());
    
            Assert.assertEquals("2.2.1 - Timing Adjustable", actual.getValues().get(1).getLabel());
            Assert.assertEquals("2.2.1", actual.getValues().get(1).getData());
    
            Assert.assertEquals("unknown", actual.getValues().get(2).getLabel());
            Assert.assertEquals("unknown", actual.getValues().get(2).getData());
        }
    }

    @Test
    public void testPopulateCheckers() throws OutputProcessorException {
        for (FailureType type: FailureType.values()) {
            Category c = new Category(null, null);
            c.getValues().add(new CategoryValue(DocumentTitleChecker.class.getSimpleName(), DocumentTitleChecker.class.getSimpleName(), 0, null, null, false));
            c.getValues().add(new CategoryValue(PDFTitleChecker.class.getSimpleName(), PDFTitleChecker.class.getSimpleName(), 0, null, null, false));
            c.getValues().add(new CategoryValue("unknown", "unknown", 0, null, null, false));
            
            Facet f = new Facet(Metadata.getMetadataClass(Metadata.Names.issueTypes(type).getName()));
            f.getCategories().add(c);
            transaction.getResponse().getFacets().add(f);
            
            processor.processOutput(transaction);
            
            Category actual = transaction.getResponse().getFacets().get(0).getCategories().get(0);
            
            Assert.assertEquals("Document does not contain a valid non-empty title element.", actual.getValues().get(0).getLabel());
            Assert.assertEquals(DocumentTitleChecker.class.getSimpleName(), actual.getValues().get(0).getData());
    
            Assert.assertEquals("PDF document is missing a title.", actual.getValues().get(1).getLabel());
            Assert.assertEquals(PDFTitleChecker.class.getSimpleName(), actual.getValues().get(1).getData());
    
            Assert.assertEquals("unknown", actual.getValues().get(2).getLabel());
            Assert.assertEquals("unknown", actual.getValues().get(2).getData());
        }
    }

}
