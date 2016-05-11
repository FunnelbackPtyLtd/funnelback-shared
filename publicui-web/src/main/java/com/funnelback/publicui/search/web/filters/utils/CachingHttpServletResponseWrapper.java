package com.funnelback.publicui.search.web.filters.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * An implementation of HttpServletResponse which captures all writes, allowing
 * them to be seen by running toByteArray.
 * 
 * All other method calls are passed directly though to the underlying
 * HttpServletResponse.
 * 
 * Based on http://stackoverflow.com/a/14741213/797
 */
public class CachingHttpServletResponseWrapper extends HttpServletResponseWrapper {
    private static class ByteArrayServletStream extends ServletOutputStream {
        ByteArrayOutputStream baos;

        ByteArrayServletStream(ByteArrayOutputStream baos) {
            this.baos = baos;
        }

        public void write(int param) throws IOException {
            baos.write(param);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            // I'm unclear how we're supposed to use the writeListener,
            // but I suspect it's relevant only if isReady ever returns false.
        }
    }

    private static class ByteArrayPrintWriter {

        private ByteArrayOutputStream baos = new ByteArrayOutputStream();

        private PrintWriter pw = new PrintWriter(baos);

        private ServletOutputStream sos = new ByteArrayServletStream(baos);

        public PrintWriter getWriter() {
            return pw;
        }

        public ServletOutputStream getStream() {
            return sos;
        }

        byte[] toByteArray() {
            return baos.toByteArray();
        }
    }

    private ByteArrayPrintWriter output;
    private boolean usingWriter;

    public CachingHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
        usingWriter = false;
        output = new ByteArrayPrintWriter();
    }

    public byte[] getByteArray() {
        return output.toByteArray();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        // As I understand, this prevents the outputStream and the writer being
        // used simultaneously - See http://stackoverflow.com/a/14741213/797
        if (usingWriter) {
            super.getOutputStream();
        }
        usingWriter = true;
        return output.getStream();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        // As I understand, this prevents the outputStream and the writer being
        // used simultaneously - See http://stackoverflow.com/a/14741213/797
        if (usingWriter) {
            super.getWriter();
        }
        usingWriter = true;
        return output.getWriter();
    }

    public String toString() {
        return output.toString();
    }
}