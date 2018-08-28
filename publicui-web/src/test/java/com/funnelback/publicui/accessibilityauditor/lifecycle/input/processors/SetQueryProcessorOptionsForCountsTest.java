package com.funnelback.publicui.accessibilityauditor.lifecycle.input.processors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

public class SetQueryProcessorOptionsForCountsTest {

    @Test
    public void test() throws Exception {
        SetQueryProcessorOptionsForCounts setOpts = new SetQueryProcessorOptionsForCounts();
        
        AccessibilityAuditorDaatOption aaDaatOpt = mock(AccessibilityAuditorDaatOption.class);
        when(aaDaatOpt.getDaatOption(any())).thenReturn("-daat-opt");
        setOpts.setAccessibilityAuditorDaatOption(aaDaatOpt);
        
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), null);
        
        setOpts.processAccessibilityAuditorTransaction(st);
        
        List<String> qpOpts = st.getQuestion().getDynamicQueryProcessorOptions();
        Assert.assertTrue(qpOpts.contains("-daat-opt"));
        
        SearchTransaction st2 = new SearchTransaction(new SearchQuestion(), null);
        setOpts.processAccessibilityAuditorTransaction(st2);
        
        Assert.assertEquals("Each time this is called the number of QP options should stay the same",
            qpOpts.size(), st2.getQuestion().getDynamicQueryProcessorOptions().size());
    }
}
