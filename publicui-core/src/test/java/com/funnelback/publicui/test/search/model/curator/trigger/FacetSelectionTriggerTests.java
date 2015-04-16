package com.funnelback.publicui.test.search.model.curator.trigger;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.curator.trigger.FacetSelectionTrigger;
import com.funnelback.publicui.search.model.curator.trigger.StringMatchType;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class FacetSelectionTriggerTests {

    @Test
    public void testFacetSelectionTriggerTrigger() {
        FacetSelectionTrigger fst = new FacetSelectionTrigger("facetname", "categoryname", StringMatchType.EXACT);
        
        SearchQuestion question = new SearchQuestion();
        SearchTransaction st = new SearchTransaction(question, null);
        
        question.getRawInputParameters().remove("f.facetname|foo");
        Assert.assertFalse("Expected to fail because facet param is not set", fst.activatesOn(st));

        question.getRawInputParameters().put("f.facetname|foo", new String[]{});
        Assert.assertFalse("Expected to fail because facet param is empty", fst.activatesOn(st));

        question.getRawInputParameters().put("f.facetname|foo", new String[]{"foo"});
        Assert.assertFalse("Expected to fail because categoryname does not match", fst.activatesOn(st));

        question.getRawInputParameters().put("f.facetname|foo", new String[]{"categoryname"});
        Assert.assertTrue("Expected to succeed because categoryname matches", fst.activatesOn(st));

        question.getRawInputParameters().put("f.facetname|foo", new String[]{"foo", "categoryname"});
        Assert.assertTrue("Expected to succeed even though categoryname is second", fst.activatesOn(st));
    }
}
