package ${package};

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.transaction.testutils.TestableSearchTransaction;
import com.funnelback.plugin.search.mock.MockSearchLifeCycleContext;

public class _ClassNamePrefix_SearchLifeCyclePluginTest {

    @Test
    public void testSearchLifeCyclePlugin(){
        TestableSearchTransaction searchTransaction = new TestableSearchTransaction()
            .withResult(Result.builder().title("hello").liveUrl("http://example.com/").build());
        MockSearchLifeCycleContext mockSearchLifeCycleContext = new MockSearchLifeCycleContext();
        // Update this to call the method(s) that should be tested. 
        new _ClassNamePrefix_SearchLifeCyclePlugin().postDatafetch(mockSearchLifeCycleContext, searchTransaction);
        
        Assertions.assertEquals("hello", searchTransaction.getResponse().getResultPacket().getResults().get(0).getTitle(),
            "Change this assert statement to check something useful");
    }
}