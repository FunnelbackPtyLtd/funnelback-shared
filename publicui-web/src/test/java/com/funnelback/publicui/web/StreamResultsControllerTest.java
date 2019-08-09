package com.funnelback.publicui.web;

import com.funnelback.publicui.search.web.controllers.StreamResultsController;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletResponse;

public class StreamResultsControllerTest {
    private StreamResultsController controller;

    HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);

    @Before
    public void init() {
        this.controller = new StreamResultsController();
    }

    @Test
    public void testAddContentDispositionHeader() {
        String fileName = "cats.csv";
        this.controller.addContentDispositionHeader(this.resp, fileName);

        Mockito.verify(this.resp, Mockito.times(1))
            .setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));
    }

    @Test
    public void testAddContentDispositionHeaderWithNoFilename() {
        this.controller.addContentDispositionHeader(this.resp, "");

        Mockito.verifyZeroInteractions(this.resp);
    }

    @Test
    public void testAddContentDispositionHeaderWithInvalidFilename() {
        this.controller.addContentDispositionHeader(this.resp, null);

        Mockito.verifyZeroInteractions(this.resp);
    }
}
