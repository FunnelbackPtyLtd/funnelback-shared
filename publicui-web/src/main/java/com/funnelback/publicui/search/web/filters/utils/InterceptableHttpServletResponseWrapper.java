package com.funnelback.publicui.search.web.filters.utils;

import java.io.IOException;
import java.io.OutputStream;
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
public class InterceptableHttpServletResponseWrapper extends HttpServletResponseWrapper {
    private static class ByteArrayServletStream extends ServletOutputStream {
        OutputStream baos;

        ByteArrayServletStream(OutputStream baos) {
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

        ByteArrayPrintWriter (OutputStream os) {
            baos = os;
            pw = new PrintWriter(baos);
            sos = new ByteArrayServletStream(baos);
        }
        
        private OutputStream baos;

        private PrintWriter pw;

        private ServletOutputStream sos;

        public PrintWriter getWriter() {
            return pw;
        }

        public ServletOutputStream getStream() {
            return sos;
        }
    }

    private ByteArrayPrintWriter output;
    private boolean usingWriter;

    public InterceptableHttpServletResponseWrapper(HttpServletResponse response, OutputStream os) {
        super(response);
        usingWriter = false;
        output = new ByteArrayPrintWriter(os);
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