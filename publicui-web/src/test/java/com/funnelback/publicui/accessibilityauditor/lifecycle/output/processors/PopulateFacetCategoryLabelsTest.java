package com.funnelback.publicui.accessibilityauditor.lifecycle.output.processors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.funnelback.common.facetednavigation.models.FacetConstraintJoin;
import com.funnelback.common.facetednavigation.models.FacetSelectionType;
import com.funnelback.common.facetednavigation.models.FacetValues;
import com.funnelback.common.filter.accessibility.Metadata;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.model.transaction.Facet;
import com.funnelback.publicui.search.model.transaction.Facet.Category;
import com.funnelback.publicui.search.model.transaction.Facet.CategoryValue;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.SearchQuestionType;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.wcag.checker.AffectedBy;
import com.funnelback.wcag.checker.FailureType;
import com.funnelback.wcag.checker.html.DocumentTitleChecker;
import com.funnelback.wcag.checker.pdf.PDFTitleChecker;
import com.funnelback.wcag.model.WCAG20Technique;
import com.funnelback.common.filter.accessibility.metadata.MetdataValueMappers.TechniquesAffectedBy;
public class PopulateFacetCategoryLabelsTest {

    private PopulateFacetCategoryLabels processor;
    
    private SearchTransaction transaction;
    
    @Before
    public void beforeEach() {
        I18n i18n = Mockito.mock(I18n.class);
        Mockito.when(i18n.tr(Mockito.anyString())).thenAnswer((invocation) -> "translated:" + invocation.getArgumentAt(0, String.class));
        
        processor = new PopulateFacetCategoryLabels();
        processor.setI18n(i18n);
        
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
        
        Facet f = new Facet(Metadata.getMetadataClass(Metadata.Names.setOfFailingPrinciples().getName()));
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
        Category c = new Category(null, null);
        c.getValues().add(new CategoryValue("123", "123", 0, null, null, false));
        c.getValues().add(new CategoryValue("221", "221", 0, null, null, false));
        c.getValues().add(new CategoryValue("unknown", "unknown", 0, null, null, false));
        
        Facet f = new Facet(Metadata.getMetadataClass(Metadata.Names.setOfFailingSuccessCriterions().getName()));
        f.getCategories().add(c);
        transaction.getResponse().getFacets().add(f);
        
        processor.processOutput(transaction);
        
        Category actual = transaction.getResponse().getFacets().get(0).getCategories().get(0);
        
        Assert.assertEquals("1.2.3 - Audio Description or Media Alternative (Prerecorded)", actual.getValues().get(0).getLabel());
        Assert.assertEquals("123", actual.getValues().get(0).getData());

        Assert.assertEquals("2.2.1 - Timing Adjustable", actual.getValues().get(1).getLabel());
        Assert.assertEquals("221", actual.getValues().get(1).getData());

        Assert.assertEquals("unknown", actual.getValues().get(2).getLabel());
        Assert.assertEquals("unknown", actual.getValues().get(2).getData());
    }

    @Test
    public void testPopulateTechniques() throws OutputProcessorException {
        for (FailureType type: FailureType.values()) {
            Category c = new Category(null, null);
            c.getValues().add(new CategoryValue(WCAG20Technique.ARIA1.name(), WCAG20Technique.ARIA1.name(), 0, null, null, false));
            c.getValues().add(new CategoryValue("unknown", "unknown", 0, null, null, false));
            
            Facet f = new Facet(Metadata.getMetadataClass(Metadata.Names.setOfFailingTechniques().getName()));
            
            f.getCategories().add(c);
            transaction.getResponse().getFacets().add(f);
            
            processor.processOutput(transaction);
            
            Category actual = transaction.getResponse().getFacets().get(0).getCategories().get(0);
            
            Assert.assertEquals("ARIA1", actual.getValues().get(0).getData());
            Assert.assertEquals("ARIA1 - " + "Using the aria-describedby property to provide a descriptive label for user interface controls", 
                actual.getValues().get(0).getLabel());
    
            Assert.assertEquals("unknown", actual.getValues().get(1).getLabel());
            Assert.assertEquals("unknown", actual.getValues().get(1).getData());
        }
    }

    @Test
    public void testPopulateAffectedBy() throws OutputProcessorException {
        for (AffectedBy by: AffectedBy.values()) {
            Category c = new Category(null, null);
            
            TechniquesAffectedBy techniquesAffectedBy = new TechniquesAffectedBy();
                
            c.getValues().add(new CategoryValue(techniquesAffectedBy.toIndexForm(by), 
                techniquesAffectedBy.toIndexForm(by), 0, null, null, false));
            c.getValues().add(new CategoryValue("unknown", "unknown", 0, null, null, false));
            
            Facet f = new Facet(Metadata.getMetadataClass(Metadata.Names.techniquesAffectedBy().getName()));
            f.getCategories().add(c);
            transaction.getResponse().getFacets().clear();
            transaction.getResponse().getFacets().add(f);
            
            processor.processOutput(transaction);
            
            Category actual = transaction.getResponse().getFacets().get(0).getCategories().get(0);
            
            Assert.assertEquals("translated:facets.label.affectedBy." + by.name(), actual.getValues().get(0).getLabel());
            Assert.assertEquals(techniquesAffectedBy.toIndexForm(by), actual.getValues().get(0).getData());
    
            Assert.assertEquals("unknown", actual.getValues().get(1).getLabel());
            Assert.assertEquals("unknown", actual.getValues().get(1).getData());
        }
    }

}
