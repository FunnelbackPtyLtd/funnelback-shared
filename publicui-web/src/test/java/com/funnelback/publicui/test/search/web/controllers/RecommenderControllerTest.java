package com.funnelback.publicui.test.search.web.controllers;

import com.funnelback.common.config.NoOptionsConfig;
import com.funnelback.publicui.recommender.web.controllers.RecommenderController;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.test.mock.MockConfigRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class RecommenderControllerTest {
    private final String DEFAULT_SEED_ITEM = "http://example.com/";

    @Autowired
    private MockConfigRepository configRepository;

    @Autowired
    private RecommenderController recommenderController;

    private MockHttpServletRequest request;

    @Before
    public void before() {
        request = new MockHttpServletRequest();
        request.setRequestURI("similarItems.json");
    }

    @Test
    public void testSimilarItems() throws Exception {
        configRepository.removeAllCollections();
        configRepository.addCollection(new Collection("dummy", new NoOptionsConfig("dummy")));
        Collection collection = new Collection("dummy", new NoOptionsConfig("dummy"));
        SearchQuestion sq = new SearchQuestion();
        sq.setCollection(collection);

        MockHttpServletResponse response = new MockHttpServletResponse();       
        //Assert.assertNull(recommenderController.similarItems(request, response, sq, null, DEFAULT_SEED_ITEM, "", 10, null));
    }
    
}
