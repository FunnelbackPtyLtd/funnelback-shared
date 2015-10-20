package com.funnelback.publicui.test.search.lifecycle.input.processors.explore;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.funnelback.collection.update.execute.utils.PadreExploreQueryGenerator;
import com.funnelback.common.config.Config;
import com.funnelback.publicui.search.lifecycle.input.processors.explore.ForkingGenerator;
import com.funnelback.publicui.search.model.collection.Collection;

public class ForkingGeneratorTests {
    
   

    @Test
    public void getExploreQueryTestExceptionThrown() throws Exception {
        Config configMocked = mock(Config.class);
        Collection collectionMocked = mock(Collection.class);
        when(collectionMocked.getConfiguration()).thenReturn(configMocked);
        String urlExpected = "theurl";
        Integer nbOfTermsExpected = 14;
        
        PadreExploreQueryGenerator padreExploreQueryGenerator = mock(PadreExploreQueryGenerator.class);
        when(padreExploreQueryGenerator.getExploreQuery(configMocked, urlExpected, nbOfTermsExpected))
            .thenAnswer(new Answer<String>() {

                @Override
                public String answer(InvocationOnMock invocation) throws Throwable {
                    throw new IOException("ignore me!");
                }
            });
        
        
        ForkingGenerator forkingGenerator = new ForkingGenerator() {
            @Override
            protected PadreExploreQueryGenerator getPadreExploreQueryGenerator() {
                return padreExploreQueryGenerator;
            }
        };
        Assert.assertNull(forkingGenerator.getExploreQuery(collectionMocked, urlExpected, nbOfTermsExpected));
    }
    
    
    @Test
    public void getExploreQueryTest() throws Exception {
        Config configMocked = mock(Config.class);
        Collection collectionMocked = mock(Collection.class);
        when(collectionMocked.getConfiguration()).thenReturn(configMocked);
        String urlExpected = "theurl";
        Integer nbOfTermsExpected = 14;
        
        PadreExploreQueryGenerator padreExploreQueryGenerator = mock(PadreExploreQueryGenerator.class);
        when(padreExploreQueryGenerator.getExploreQuery(configMocked, urlExpected, nbOfTermsExpected))
            .thenReturn(Optional.of("ans"));        
        
        ForkingGenerator forkingGenerator = new ForkingGenerator() {
            @Override
            protected PadreExploreQueryGenerator getPadreExploreQueryGenerator() {
                return padreExploreQueryGenerator;
            }
        };
        Assert.assertEquals("ans", forkingGenerator.getExploreQuery(collectionMocked, urlExpected, nbOfTermsExpected));
        
    }
    
    @Test
    public void getExploreQueryEmptyOptional() throws Exception {
        Config configMocked = mock(Config.class);
        Collection collectionMocked = mock(Collection.class);
        when(collectionMocked.getConfiguration()).thenReturn(configMocked);
        String urlExpected = "theurl";
        Integer nbOfTermsExpected = 14;
        
        PadreExploreQueryGenerator padreExploreQueryGenerator = mock(PadreExploreQueryGenerator.class);
        when(padreExploreQueryGenerator.getExploreQuery(configMocked, urlExpected, nbOfTermsExpected))
            .thenReturn(Optional.empty());        
        
        ForkingGenerator forkingGenerator = new ForkingGenerator() {
            @Override
            protected PadreExploreQueryGenerator getPadreExploreQueryGenerator() {
                return padreExploreQueryGenerator;
            }
        };
        Assert.assertNull(forkingGenerator.getExploreQuery(collectionMocked, urlExpected, nbOfTermsExpected));
        
    }
    
   
}
