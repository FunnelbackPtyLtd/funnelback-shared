package com.funnelback.publicui.accessibilityauditor.lifecycle.output.processors;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.padre.IndexedTermCounts;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;



public class ReMapRMCFOutputProcessorTest {

    @Test
    public void test() throws Exception {
        // FunAAFormat -> should be lower cased
        // FunAASetOfFailingTechniques -> should be upper cased
        // Foo -> should be ignored 
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), new SearchResponse());
        st.getResponse().setResultPacket(new ResultPacket());
        ResultPacket rp = st.getResponse().getResultPacket();
        
        IndexedTermCounts formatMd =  new IndexedTermCounts("FunAAFormat");
        formatMd.getTermAndOccurrences().put("hTmL", 123L);
        formatMd.getTermAndOccurrences().put("PdF", 12L);
        
        
        IndexedTermCounts failingTechniques =  new IndexedTermCounts("FunAASetOfFailingTechniques");
        failingTechniques.getTermAndOccurrences().put("Aria12", 12L);
        failingTechniques.getTermAndOccurrences().put("HTML12", 12L);
        
        IndexedTermCounts foo =  new IndexedTermCounts("foo");
        foo.getTermAndOccurrences().put("Bar", 12L);
        
        rp.getIndexedTermCounts().add(formatMd);
        rp.getIndexedTermCounts().add(failingTechniques);
        rp.getIndexedTermCounts().add(foo);
        
        rp.getRmcs().put("FunAASetOfFailingTechniques:HTML12", 100);
        
        new ReMapRMCFOutputProcessor().processAccessibilityAuditorTransaction(st);
        
        // FunAAFormat should be lower cased
        Assert.assertEquals(123, rp.getRmcs().get("FunAAFormat:html") + 0);
        Assert.assertEquals(12, rp.getRmcs().get("FunAAFormat:pdf") + 0);
        
        // Techniques should be upper cased
        Assert.assertEquals(12, rp.getRmcs().get("FunAASetOfFailingTechniques:ARIA12") + 0);
        
        // We should not replace RMCF values that already exist, perhaps the user 
        // wanted to user RMCF rather than the optimisation.
        Assert.assertEquals(100, rp.getRmcs().get("FunAASetOfFailingTechniques:HTML12") + 0);
        
        
        Assert.assertNull("Should ignore non remapped metadata", rp.getRmcs().get("foo:Bar"));
    }
}
