package com.funnelback.publicui.web;

import com.funnelback.publicui.search.web.controllers.StreamResultsController;
import org.junit.Assert;
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

        Mockito.when(this.resp.getHeader("Content-Disposition"))
            .thenReturn(String.format("attachment; filename=\"%s\"", fileName));
        
        String headerResult = this.resp.getHeader("Content-Disposition");
        Assert.assertEquals(
            "Content-Disposition header should be set correctly", 
            "attachment; filename=\"cats.csv\"", 
            headerResult
        );
    }

    @Test
    public void testAddContentDispositionHeaderWithNoFilename() {
        this.controller.addContentDispositionHeader(this.resp, "");

        Mockito.when(this.resp.getHeader("Content-Disposition"))
            .thenReturn("");

        String headerResult = this.resp.getHeader("Content-Disposition");
        Assert.assertEquals(
            "Content-Disposition header shouldn't exist",
            "",
            headerResult
        );
    }

    @Test
    public void testAddContentDispositionHeaderWithInvalidFilename() {
        this.controller.addContentDispositionHeader(this.resp, null);

        Mockito.when(this.resp.getHeader("Content-Disposition"))
            .thenReturn("");

        String headerResult = this.resp.getHeader("Content-Disposition");
        Assert.assertEquals(
            "Content-Disposition header shouldn't exist",
            "",
            headerResult
        );
    }
}
