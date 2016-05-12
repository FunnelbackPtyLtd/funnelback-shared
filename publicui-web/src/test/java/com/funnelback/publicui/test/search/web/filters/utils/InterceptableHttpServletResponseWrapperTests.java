package com.funnelback.publicui.test.search.web.filters.utils;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;

import com.funnelback.publicui.search.web.filters.utils.InterceptableHttpServletResponseWrapper;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class InterceptableHttpServletResponseWrapperTests {

    @Test
    public void testMethodPassThrough() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        InterceptableHttpServletResponseWrapper ihsrw = new InterceptableHttpServletResponseWrapper(mockResponse, baos);

        ihsrw.setCharacterEncoding("test-encoding");

        verify(mockResponse).setCharacterEncoding("test-encoding");
    }

    @Test
    public void testOutputStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        InterceptableHttpServletResponseWrapper ihsrw = new InterceptableHttpServletResponseWrapper(mockResponse, baos);

        ihsrw.getOutputStream().write(7);

        Assert.assertArrayEquals(new byte[] { 7 }, baos.toByteArray());
    }

    @Test
    public void testWriter() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        InterceptableHttpServletResponseWrapper ihsrw = new InterceptableHttpServletResponseWrapper(mockResponse, baos);

        ihsrw.getWriter().print("foo");
        ihsrw.getWriter().flush();

        Assert.assertArrayEquals("foo".getBytes(), baos.toByteArray());
    }

}
