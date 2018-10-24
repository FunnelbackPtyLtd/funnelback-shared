package com.funnelback.publicui.search.service;

import com.funnelback.common.profile.ProfileAndView;
import com.funnelback.common.profile.ProfileId;
import com.funnelback.common.profile.ProfileView;
import com.funnelback.publicui.search.lifecycle.SearchTransactionProcessor;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.padre.Result;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.Assert.*;

public class DefaultSampleCollectionUrlServiceTest {

    @Test
    public void testGetSampleUrl() throws SampleCollectionUrlService.CouldNotFindAnyUrlException {
        DefaultSampleCollectionUrlService service = new DefaultSampleCollectionUrlService();

        SearchTransactionProcessor searchTransactionProcessor = Mockito.mock(SearchTransactionProcessor.class);
        service.setSearchTransactionProcessor(searchTransactionProcessor);

        Result mockResult = new Result();
        mockResult.setLiveUrl("http://expected.com/");

        ResultPacket mockResultPacket = new ResultPacket();
        mockResultPacket.getResults().add(mockResult);

        SearchResponse mockResponse = new SearchResponse();
        mockResponse.setResultPacket(mockResultPacket);

        ArgumentCaptor<SearchQuestion> questionCaptor = ArgumentCaptor.forClass(SearchQuestion.class);
        Mockito.when(searchTransactionProcessor.process(questionCaptor.capture(), Mockito.any(), Mockito.any()))
            .thenReturn(new SearchTransaction(null, mockResponse));

        Collection collection = Mockito.mock(Collection.class);

        String url = service.getSampleUrl(collection, new ProfileAndView(new ProfileId("foo"), ProfileView.live));

        Assert.assertEquals("http://expected.com/", url);
    }
}