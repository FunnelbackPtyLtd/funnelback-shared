package com.funnelback.publicui.streamedresults.converters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import com.funnelback.publicui.streamedresults.DataConverter;

public class NoHeaderAndFooterDataConverterTest {

    @Test
    public void testHeaderAndFooterNotWritten() throws Exception {
        DataConverter<Object> wrapped = mock(DataConverter.class);
        NoHeaderAndFooterDataConverter<Object> converter = new NoHeaderAndFooterDataConverter<>(wrapped);
        
        converter.finished(null);
        converter.writeHead(null, null);
        converter.writeFooter(null);
        
        verify(wrapped, times(1)).finished(null);
        verify(wrapped, never()).writeHead(null, null);
        verify(wrapped, never()).writeFooter(null);
    }
}
