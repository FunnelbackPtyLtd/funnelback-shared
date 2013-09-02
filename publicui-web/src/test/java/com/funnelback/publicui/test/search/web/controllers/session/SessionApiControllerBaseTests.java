package com.funnelback.publicui.test.search.web.controllers.session;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.mock.web.MockHttpServletResponse;

import com.funnelback.publicui.search.web.controllers.session.SessionApiControllerBase;

public class SessionApiControllerBaseTests extends SessionApiControllerBase{

    private static final Map<String, String> TEST_MAP = new HashMap<>();
    static { TEST_MAP.put("a-test", "a-value"); }
    
    @Test
    public void testSendResponseNoData() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        this.sendResponse(response, 405, null);
        
        assertEquals(405, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertEquals("", response.getContentAsString());
    }

    @Test
    public void testSendResponseData() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        this.sendResponse(response, 205, TEST_MAP);
        
        assertEquals(205, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertEquals("{\"a-test\":\"a-value\"}", response.getContentAsString());
    }
    
    @Test
    public void testGetJsonErrorMap() {
        Map<String, String> map = getJsonErrorMap("This is an error");
        assertEquals(2, map.size());
        assertEquals(KO, map.get(STATUS));
        assertEquals("This is an error", map.get(ERROR_MESSAGE));
    }

}
