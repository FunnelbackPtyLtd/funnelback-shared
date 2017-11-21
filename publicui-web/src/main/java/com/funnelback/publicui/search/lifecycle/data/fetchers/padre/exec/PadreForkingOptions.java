package com.funnelback.publicui.search.lifecycle.data.fetchers.padre.exec;

public interface PadreForkingOptions {

    public long getPadreForkingTimeout();
    
    public int getPadreMaxPacketSize();
    
    public int getSizeAtWhichToCompressPackets();
    
}
