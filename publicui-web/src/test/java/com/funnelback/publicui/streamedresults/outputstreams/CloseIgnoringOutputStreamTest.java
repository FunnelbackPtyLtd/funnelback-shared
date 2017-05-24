package com.funnelback.publicui.streamedresults.outputstreams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;


public class CloseIgnoringOutputStreamTest {

    @Test
    public void TestCloseDoesNotCloseParentStream() throws Exception {
        AtomicBoolean closeCalled = new AtomicBoolean(false);
        new CloseIgnoringOutputStream(new DelegateOutputStream(new ByteArrayOutputStream()) {
            @Override
            public void close() throws IOException {
                closeCalled.set(true);
            }
        }).close();
        
        Assert.assertFalse(closeCalled.get());
    }
}
