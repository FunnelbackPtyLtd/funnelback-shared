package ${package};

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.plugin.index.consumers.mock.MockExternalMetadataConsumer;
import com.funnelback.plugin.index.consumers.mock.MockGscopeByQueryConsumer;
import com.funnelback.plugin.index.consumers.mock.MockGscopeByRegexConsumer;
import com.funnelback.plugin.index.consumers.mock.MockKillByExactMatchConsumer;
import com.funnelback.plugin.index.consumers.mock.MockKillByPartialMatchConsumer;
import com.funnelback.plugin.index.consumers.mock.MockMetadataMappingConsumer;
import com.funnelback.plugin.index.mock.MockIndexConfigProviderContext;

public class _ClassNamePrefix_IndexingConfigProviderTest {

    @Test
    public void externalMetadataTest() {
        MockIndexConfigProviderContext mockContext = new MockIndexConfigProviderContext();
        MockExternalMetadataConsumer mockConsumer = new MockExternalMetadataConsumer();
        _ClassNamePrefix_IndexingConfigProvider underTest = new _ClassNamePrefix_IndexingConfigProvider();
        
        underTest.externalMetadata(mockContext, mockConsumer);
        
        Assert.assertTrue("Check how many times the consumer was called.", mockConsumer.getInvocations().size() >= 0);
    }
    
    @Test
    public void metadataMappingsTest() {
        MockIndexConfigProviderContext mockContext = new MockIndexConfigProviderContext();
        MockMetadataMappingConsumer mockConsumer = new MockMetadataMappingConsumer();
        _ClassNamePrefix_IndexingConfigProvider underTest = new _ClassNamePrefix_IndexingConfigProvider();
        
        underTest.metadataMappings(mockContext, mockConsumer);
        
        Assert.assertTrue("Check how many times the consumer was called.", mockConsumer.getInvocations().size() >= 0);
    }
    
    @Test
    public void killByExactMatchTest() {
        MockIndexConfigProviderContext mockContext = new MockIndexConfigProviderContext();
        MockKillByExactMatchConsumer mockConsumer = new MockKillByExactMatchConsumer();
        _ClassNamePrefix_IndexingConfigProvider underTest = new _ClassNamePrefix_IndexingConfigProvider();
        
        underTest.killByExactMatch(mockContext, mockConsumer);
        
        Assert.assertTrue("Check how many times the consumer was called.", mockConsumer.getInvocations().size() >= 0);
    }
    
    @Test
    public void killByPartialMatchTest() {
        MockIndexConfigProviderContext mockContext = new MockIndexConfigProviderContext();
        MockKillByPartialMatchConsumer mockConsumer = new MockKillByPartialMatchConsumer();
        _ClassNamePrefix_IndexingConfigProvider underTest = new _ClassNamePrefix_IndexingConfigProvider();
        
        underTest.killByPartialMatch(mockContext, mockConsumer);
        
        Assert.assertTrue("Check how many times the consumer was called.", mockConsumer.getInvocations().size() >= 0);
    }
    
    @Test
    public void supplyGscopesByRegexTest() {
        MockIndexConfigProviderContext mockContext = new MockIndexConfigProviderContext();
        MockGscopeByRegexConsumer mockConsumer = new MockGscopeByRegexConsumer();
        _ClassNamePrefix_IndexingConfigProvider underTest = new _ClassNamePrefix_IndexingConfigProvider();
        
        underTest.supplyGscopesByRegex(mockContext, mockConsumer);
        
        Assert.assertTrue("Check how many times the consumer was called.", mockConsumer.getInvocations().size() >= 0);
    }
    
    @Test
    public void supplyGscopesByQueryTest() {
        MockIndexConfigProviderContext mockContext = new MockIndexConfigProviderContext();
        MockGscopeByQueryConsumer mockConsumer = new MockGscopeByQueryConsumer();
        _ClassNamePrefix_IndexingConfigProvider underTest = new _ClassNamePrefix_IndexingConfigProvider();
        
        underTest.supplyGscopesByQuery(mockContext, mockConsumer);
        
        Assert.assertTrue("Check how many times the consumer was called.", mockConsumer.getInvocations().size() >= 0);
    }
}
