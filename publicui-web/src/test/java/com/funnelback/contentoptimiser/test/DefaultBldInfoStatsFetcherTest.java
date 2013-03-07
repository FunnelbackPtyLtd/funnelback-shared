package com.funnelback.contentoptimiser.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import com.funnelback.contentoptimiser.fetchers.impl.DefaultBldInfoStatsFetcher;
import com.funnelback.contentoptimiser.processors.impl.BldInfoStats;
import com.funnelback.publicui.i18n.I18n;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.contentoptimiser.ContentOptimiserModel;
import com.funnelback.publicui.search.service.IndexRepository;

public class DefaultBldInfoStatsFetcherTest {
    
    @Test
    public void testComponentCollection() throws IOException {
        DefaultBldInfoStatsFetcher fetcher = new DefaultBldInfoStatsFetcher();
        
        IndexRepository indexRepo = mock(IndexRepository.class);
        when(indexRepo.getBuildInfoValue("collection_id", IndexRepository.BuildInfoKeys.Num_docs.toString())).thenReturn("100");
        when(indexRepo.getBuildInfoValue("collection_id", IndexRepository.BuildInfoKeys.Average_document_length.toString())).thenReturn("10 content words");        
        
        fetcher.setIndexRepository(indexRepo);
        Collection c = mock(Collection.class);
        when(c.getId()).thenReturn("collection_id");
        when(c.getMetaComponents()).thenReturn(new String[] {});
        
        ContentOptimiserModel model = new ContentOptimiserModel();
        
        BldInfoStats s = fetcher.fetch(model, c);
        Assert.assertEquals("Should be no error messages", 0,model.getMessages().size());
        Assert.assertEquals("Should have correct bldInfo values",10,s.getAvgWords());
        Assert.assertEquals("Should have correct bldInfo values",100,s.getTotalDocuments());
    }

    @Test
    public void testComponentCollectionWithNoDocuments() throws IOException {
        DefaultBldInfoStatsFetcher fetcher = new DefaultBldInfoStatsFetcher();
        
        IndexRepository indexRepo = mock(IndexRepository.class);
        when(indexRepo.getBuildInfoValue("collection_id", IndexRepository.BuildInfoKeys.Num_docs.toString())).thenReturn("0");
        when(indexRepo.getBuildInfoValue("collection_id", IndexRepository.BuildInfoKeys.Average_document_length.toString())).thenReturn("0 content words");        
        
        I18n i18n = mock(I18n.class);
        when(i18n.tr("error.readingBldinfo")).thenReturn("error.readingBldinfo");
        
        fetcher.setIndexRepository(indexRepo);
        fetcher.setI18n(i18n);
        Collection c = mock(Collection.class);
        when(c.getId()).thenReturn("collection_id");
        when(c.getMetaComponents()).thenReturn(new String[] {});
        
        ContentOptimiserModel model = new ContentOptimiserModel();
        
        BldInfoStats s = fetcher.fetch(model, c);
        Assert.assertEquals("Should be an error message", 1,model.getMessages().size());
        Assert.assertEquals("error.readingBldinfo",model.getMessages().get(0));
        Assert.assertEquals("Should have correct bldInfo values",0,s.getAvgWords());
        Assert.assertEquals("Should have correct bldInfo values",0,s.getTotalDocuments());
    }

    @Test
    public void testMetaCollection() throws IOException {
        DefaultBldInfoStatsFetcher fetcher = new DefaultBldInfoStatsFetcher();
        
        IndexRepository indexRepo = mock(IndexRepository.class);
        when(indexRepo.getBuildInfoValue("collection_1", IndexRepository.BuildInfoKeys.Num_docs.toString())).thenReturn("100");
        when(indexRepo.getBuildInfoValue("collection_1", IndexRepository.BuildInfoKeys.Average_document_length.toString())).thenReturn("20 content words");        

        when(indexRepo.getBuildInfoValue("collection_2", IndexRepository.BuildInfoKeys.Num_docs.toString())).thenReturn("300");
        when(indexRepo.getBuildInfoValue("collection_2", IndexRepository.BuildInfoKeys.Average_document_length.toString())).thenReturn("10.0 content words");        
            
        fetcher.setIndexRepository(indexRepo);
    
        Collection c = mock(Collection.class);
        when(c.getId()).thenReturn("collection_id");
        when(c.getMetaComponents()).thenReturn(new String[] {"collection_1","collection_2"});
        
        ContentOptimiserModel model = new ContentOptimiserModel();
        
        BldInfoStats s = fetcher.fetch(model, c);
        Assert.assertEquals("Should be no error messages", 0,model.getMessages().size());
        Assert.assertEquals("Should have correct bldInfo values",12,s.getAvgWords());
        Assert.assertEquals("Should have correct bldInfo values",400,s.getTotalDocuments());
    }

    
}
