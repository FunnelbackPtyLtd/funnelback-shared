package com.funnelback.publicui.test.search.lifecycle.output.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.funnelback.config.configtypes.service.DefaultServiceConfig;
import com.funnelback.config.configtypes.service.ServiceConfig;
import com.funnelback.config.data.InMemoryConfigData;
import com.funnelback.config.data.environment.NoConfigEnvironment;
import com.funnelback.publicui.search.model.collection.Profile;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.publicui.search.lifecycle.input.processors.PassThroughEnvironmentVariables;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.SearchHistory;
import com.funnelback.publicui.search.model.padre.Error;
import com.funnelback.publicui.search.model.padre.ResultPacket;
import com.funnelback.publicui.search.model.padre.ResultsSummary;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.model.transaction.session.SearchSession;
import com.funnelback.publicui.search.service.SearchHistoryRepository;
import com.funnelback.publicui.test.mock.MockConfigRepository;
import com.funnelback.publicui.test.search.service.session.SessionDaoTest;

import static com.funnelback.config.keys.Keys.FrontEndKeys;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class SearchHistoryTests extends SessionDaoTest {

    @Autowired
    private MockConfigRepository configRepository;
    
    @Autowired
    private SearchHistoryRepository repository;

    private SearchHistory processor;
    
    private SearchTransaction st;
    private ServiceConfig serviceConfig;

    @Override
    public void before() throws Exception {
        configRepository.addCollection(collection);
        
        processor = new SearchHistory();
        processor.setSearchHistoryRepository(repository);
        serviceConfig = new DefaultServiceConfig(new InMemoryConfigData(Maps.newHashMap()), new NoConfigEnvironment());

        Profile profile = new Profile("_default");
        profile.setServiceConfig(serviceConfig);
        collection.getProfiles().put("_default", profile);

        SearchQuestion sq = new SearchQuestion();
        sq.setCurrentProfile("_default");
        sq.setCollection(collection);

        st = new SearchTransaction(sq, new SearchResponse());
        st.getQuestion().setCollection(collection);
        st.getQuestion().setOriginalQuery("original query");
        st.getQuestion().getInputParameterMap()
            .put(PassThroughEnvironmentVariables.Keys.REQUEST_URL.toString(),
                "http://server.com/s/search.html?collection="+collection.getId()+"&query=original+query&myparam=myvalue");
        
        st.getResponse().setResultPacket(new ResultPacket());
        st.getResponse().getResultPacket().setResultsSummary(new ResultsSummary());
        st.getResponse().getResultPacket().getResultsSummary().setCurrStart(42);
        st.getResponse().getResultPacket().getResultsSummary().setNumRanks(36);
        st.getResponse().getResultPacket().getResultsSummary().setTotalMatching(1234);
        st.getResponse().getResultPacket().setQueryAsProcessed("query as processed");

        st.setSession(new SearchSession(user));
    }
    
    @Test
    public void testMissingData() throws Exception {
        // No transaction
        processor.processOutput(null);
        
        // No response & question
        processor.processOutput(new SearchTransaction(null, null));
        
        // No question
        processor.processOutput(new SearchTransaction(null, new SearchResponse()));
        
        // No response
        processor.processOutput(new SearchTransaction(new SearchQuestion(), null));
        
        // No results
        SearchResponse response = new SearchResponse();
        processor.processOutput(new SearchTransaction(null, response));
        
        // No results in packet
        response.setResultPacket(new ResultPacket());
        processor.processOutput(new SearchTransaction(null, response));
        
        // No query
        SearchTransaction st = new SearchTransaction(new SearchQuestion(), response);
        st.getResponse().getResultPacket().setQuery(null);
        processor.processOutput(st);
    }
    
    @Test
    public void testSessionDisabled() throws OutputProcessorException {
        serviceConfig.set(FrontEndKeys.ModernUi.Session.SESSION, false);
        assertEquals(0, repository.getSearchHistory(user, collection, 10).size());
        processor.processOutput(st);
        assertEquals(0, repository.getSearchHistory(user, collection, 10).size());
    }
    
    
    @Test
    public void testLogQuerySetFalse() throws OutputProcessorException {
        serviceConfig.set(FrontEndKeys.ModernUi.Session.SESSION, true);
        assertEquals(0, repository.getSearchHistory(user, collection, 10).size());
        st.getQuestion().setLogQuery(Optional.of(false));
        processor.processOutput(st);
        assertEquals(0, repository.getSearchHistory(user, collection, 10).size());
    }
    
    @Test
    public void testLogQuerySetTrue() throws OutputProcessorException {
        serviceConfig.set(FrontEndKeys.ModernUi.Session.SESSION, true);
        assertEquals(0, repository.getSearchHistory(user, collection, 10).size());
        st.getQuestion().setLogQuery(Optional.of(true));
        processor.processOutput(st);
        assertEquals(1, repository.getSearchHistory(user, collection, 10).size());
    }
    
    @Test
    public void testLogQuerySetEmpty() throws OutputProcessorException {
        serviceConfig.set(FrontEndKeys.ModernUi.Session.SESSION, true);
        assertEquals(0, repository.getSearchHistory(user, collection, 10).size());
        st.getQuestion().setLogQuery(Optional.empty());
        processor.processOutput(st);
        assertEquals(1, repository.getSearchHistory(user, collection, 10).size());
    }
    
    @Test
    public void test() throws OutputProcessorException {
        serviceConfig.set(FrontEndKeys.ModernUi.Session.SESSION, true);
        assertEquals(0, repository.getSearchHistory(user, collection, 10).size());
        processor.processOutput(st);
        
        List<com.funnelback.publicui.search.model.transaction.session.SearchHistory> history = repository.getSearchHistory(user, collection, 10);
        assertEquals(1, history.size());
        
        com.funnelback.publicui.search.model.transaction.session.SearchHistory sh = history.get(0);
        
        assertEquals(collection.getId(), sh.getCollection());
        assertEquals(42, sh.getCurrStart());
        assertEquals(36, sh.getNumRanks());
        assertEquals("original query", sh.getOriginalQuery());
        assertEquals("query as processed", sh.getQueryAsProcessed());
        
        assertEquals("collection="+collection.getId()+"&query=original+query&myparam=myvalue", sh.getSearchParams());
        assertNotSame(0, sh.getSearchParamsSignature());
        assertEquals(1234, sh.getTotalMatching());
        assertEquals(user.getId(), sh.getUserId());

        // Date should be very recent
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, -1);
        assertTrue(sh.getSearchDate().after(c.getTime()));
        assertTrue(sh.getSearchDate().before(new Date()));
    }
    
    @Test
    public void testNoResultPacket() throws OutputProcessorException {
        st.getResponse().setResultPacket(null);
        serviceConfig.set(FrontEndKeys.ModernUi.Session.SESSION, true);
        assertEquals(0, repository.getSearchHistory(user, collection, 10).size());
        processor.processOutput(st);
        assertEquals(0, repository.getSearchHistory(user, collection, 10).size());
    }

    @Test
    public void testErrorInSearch() throws OutputProcessorException {
        st.getResponse().getResultPacket().setError(new Error());
        serviceConfig.set(FrontEndKeys.ModernUi.Session.SESSION, true);
        assertEquals(0, repository.getSearchHistory(user, collection, 10).size());
        processor.processOutput(st);
        assertEquals(0, repository.getSearchHistory(user, collection, 10).size());
    }

    @Test
    public void testInvalidURL() throws OutputProcessorException {
        serviceConfig.set(FrontEndKeys.ModernUi.Session.SESSION, true);
        st.getQuestion().getInputParameterMap()
            .put(PassThroughEnvironmentVariables.Keys.REQUEST_URL.toString(),
                "this is not a valid URL, obviously");

        assertEquals(0, repository.getSearchHistory(user, collection, 10).size());
        processor.processOutput(st);
        assertEquals(0, repository.getSearchHistory(user, collection, 10).size());
    }

    /**
     * @see FUN-8054
     * @throws OutputProcessorException
     */
    @Test
    public void testNoOriginalQuery() throws OutputProcessorException {
        serviceConfig.set(FrontEndKeys.ModernUi.Session.SESSION, true);
        st.getQuestion().setOriginalQuery(null);
        processor.processOutput(st);
        
        List<com.funnelback.publicui.search.model.transaction.session.SearchHistory> history = repository.getSearchHistory(user, collection, 10);
        assertEquals(1, history.size());
        
        assertNull("Original query should be permitted to be NULL", history.get(0).getOriginalQuery());
    }    

}
