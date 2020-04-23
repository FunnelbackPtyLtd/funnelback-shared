package __fixed_package__;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.testutils.TestableSearchTransaction;

public class _ClassNamePrefix_SearchLifeCyclePluginTest {

    @Test
    public void testSearchLifeCyclePlugin(){
        TestableSearchTransaction searchTransaction = new TestableSearchTransaction()
            .withResult(Result.builder().title("hello").liveUrl("http://example.com/").build());
        
        // Update this to call the method(s) that should be tested. 
        new _ClassNamePrefix_SearchLifeCyclePlugin().postDatafetch(searchTransaction);
        
        Assert.assertEquals("Change this assert statement to check something useful",
            "hello", searchTransaction.getResponse().getResultPacket().getResults().get(0).getTitle());
    }
    
}
