package com.funnelback.publicui.test.search.web.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.funnelback.common.config.DefaultValues;
import com.funnelback.common.config.ProfileId;
import com.funnelback.publicui.search.web.controllers.SuggestController;
import com.funnelback.publicui.test.mock.MockConfigRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/test/resources/spring/applicationContext.xml")
public class SuggestControllerTest {

    @Autowired
    private MockConfigRepository configRepository;

    private SuggestController suggestController;
    
    @Before
    public void before() {
        suggestController = new SuggestController();
        suggestController.setConfigRepository(configRepository);
    }
    
    @Test
    public void testNoCollection() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        suggestController.noCollection(response);
        Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }
    
    @Test
    public void testInvalidCollection() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        suggestController.suggestJava(null,
                new ProfileId(DefaultValues.DEFAULT_PROFILE),
                "ab", 0, 0, "json", 0.5, "abc", "cb", null, new MockHttpServletRequest(), response);
        
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }
    
}
