package com.funnelback.publicui.streamedresults.outputstreams;

import java.io.OutputStream;

import lombok.AllArgsConstructor;
import lombok.Delegate;
import lombok.NonNull;

@AllArgsConstructor
public class DelegateOutputStream extends OutputStream {
    @NonNull @Delegate final private OutputStream outputStream;
}
