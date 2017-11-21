package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec;

import com.funnelback.publicui.utils.BoundedByteArrayOutputStream;
import com.funnelback.publicui.utils.CompressingByteArrayOutputStream;

public class PadreOuputHelper {

    public BoundedByteArrayOutputStream getOupputStreamForPadre(int estimatedSize, PadreForkingOptions padreForkingOptions) {
        CompressingByteArrayOutputStream compressingOS = CompressingByteArrayOutputStream.builder()
                .withInitialByteArraySize(estimatedSize)
                .withCompressAfterSize(padreForkingOptions.getSizeAtWhichToCompressPackets())
                .build();
        return new BoundedByteArrayOutputStream(compressingOS, padreForkingOptions.getPadreMaxPacketSize());
    }
    
}
