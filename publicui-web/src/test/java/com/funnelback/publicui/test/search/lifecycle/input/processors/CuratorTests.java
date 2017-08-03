package com.funnelback.publicui.test.search.lifecycle.input.processors;

import java.io.FileNotFoundException;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.input.processors.Curator;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;
import com.funnelback.publicui.search.service.config.DefaultConfigRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class CuratorTests {

    @Resource(name="localConfigRepository")
    private DefaultConfigRepository configRepository;

    private static final String COLLECTION_ID = "curator";
    
    private SearchQuestion question;

    @Before
    public void before() throws Exception {
        question = new SearchQuestion();
        question.setCollection(configRepository.getCollection(COLLECTION_ID));
        question.setQuery("uniquequery");
    }

    @Test
    public void testCuratorInputProcessor() throws FileNotFoundException, InputProcessorException {
        question.setProfile("profile1");
        question.setCurrentProfile("profile1");

        SearchTransaction st = new SearchTransaction(question, new SearchResponse());

        Curator processor = new Curator();
        processor.processInput(st);

        Assert.assertEquals("uniqueurl",
            st.getQuestion().getAdditionalParameters().get(RequestParameters.PROMOTE_URLS)[0]);

        Assert.assertEquals("Expected no exhibits (done by output processor).", 0, st.getResponse().getCurator().getExhibits().size());
    }

    @Test
    public void testCuratorInputProcessorMissingProfile() throws FileNotFoundException, InputProcessorException {
        question.setProfile("profile-missing");
        question.setCurrentProfile("profile-missing");

        SearchTransaction st = new SearchTransaction(question, null);

        Curator processor = new Curator();
        processor.processInput(st);

        Assert.assertFalse("There should be no URLs promoted when no valid profile is in use.",
            st.getQuestion().getAdditionalParameters().containsKey(RequestParameters.PROMOTE_URLS));
    }
}
