package com.funnelback.publicui.search.web.views;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class FunnelbackMappingJackson2JsonViewTest {
    private FunnelbackMappingJackson2JsonView view;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        view = new FunnelbackMappingJackson2JsonView();
    }

    @Test
    public void renderWithJsonp() throws Exception {
        testJsonp("jsonp", "callback", true);
        testJsonp("jsonp", "_callback", true);
        testJsonp("jsonp", "_Call.bAcK", true);
        testJsonp("jsonp", "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_.", true);

        testJsonp("jsonp", "<script>", false);
        testJsonp("jsonp", "!foo!bar", false);
    }

    private void testJsonp(String paramName, String paramValue, boolean validValue) throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("foo", "bar");

        this.request = new MockHttpServletRequest();
        this.request.addParameter("otherparam", "value");
        this.request.addParameter(paramName, paramValue);
        this.response = new MockHttpServletResponse();

        this.view.render(model, this.request, this.response);

        String content = this.response.getContentAsString();
        if (validValue) {
            assertEquals("/**/" + paramValue + "({\"foo\":\"bar\"});", content);
        }
        else {
            assertEquals("{\"foo\":\"bar\"}", content);
        }
    }
}