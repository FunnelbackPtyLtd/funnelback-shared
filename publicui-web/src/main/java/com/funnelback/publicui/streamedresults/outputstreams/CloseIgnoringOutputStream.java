package com.funnelback.publicui.streamedresults.outputstreams;

import java.io.IOException;
import java.io.OutputStream;

import lombok.extern.log4j.Log4j2;

/**
 * A OutputStream that when close is called close() is not called on the underlying stream.
 *
 */
@Log4j2
public class CloseIgnoringOutputStream extends DelegateOutputStream {

    public CloseIgnoringOutputStream(OutputStream outputStream) {
        super(outputStream);
    }

    @Override
    public void close() throws IOException {
       log.debug("Tsk tsk tsk, closed was called when it shouldn't have been."); 
    }
}
