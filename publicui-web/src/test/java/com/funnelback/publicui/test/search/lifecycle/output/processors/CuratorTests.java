package com.funnelback.publicui.test.search.lifecycle.output.processors;

import java.io.FileNotFoundException;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.publicui.search.lifecycle.input.InputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.OutputProcessorException;
import com.funnelback.publicui.search.lifecycle.output.processors.Curator;
import com.funnelback.publicui.search.model.transaction.SearchQuestion;
import com.funnelback.publicui.search.model.transaction.SearchQuestion.RequestParameters;
import com.funnelback.publicui.search.model.transaction.SearchResponse;
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
    public void testCuratorOutputProcessor() throws FileNotFoundException, InputProcessorException, OutputProcessorException {
        question.setProfile("profile1");
        question.setCurrentProfile("profile1");

        SearchTransaction st = new SearchTransaction(question, new SearchResponse());

        Curator processor = new Curator();
        processor.processOutput(st);

        Assert.assertFalse("There should be no URLs promoted (that's done by the input processor).",
            st.getQuestion().getAdditionalParameters().containsKey(RequestParameters.PROMOTE_URLS));

        Assert.assertEquals("Expected exactly one exhibit (a message).", 1, st.getResponse().getCurator().getExhibits().size());
    }

    @Test
    public void testCuratorOutputProcessorMissingProfile() throws FileNotFoundException, InputProcessorException, OutputProcessorException {
        question.setProfile("profile-missing");
        question.setCurrentProfile("profile-missing");

        SearchTransaction st = new SearchTransaction(question, new SearchResponse());

        Curator processor = new Curator();
        processor.processOutput(st);

        Assert.assertFalse("There should be no URLs promoted when no valid profile is in use.",
            st.getQuestion().getAdditionalParameters().containsKey(RequestParameters.PROMOTE_URLS));
    }
}
